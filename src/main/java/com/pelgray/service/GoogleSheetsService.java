package com.pelgray.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendCellsRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BooleanCondition;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.pelgray.domain.SheetColumn;
import com.pelgray.domain.Vacancy;
import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.exceptions.GoogleRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GoogleSheetsService {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsService.class);
    private static Sheets.Spreadsheets sheetsGateway;
    private static Credential credential;
    /**
     * Id Google таблицы
     */
    private final String spreadsheetId;

    GoogleSheetsService(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public static GoogleSheetsService createService(String spreadsheetId)
            throws GoogleRequestException, GoogleConnectionException {
        GoogleSheetsService result = new GoogleSheetsService(spreadsheetId);
        initToken();
        result.updateHeaders();
        return result;
    }

    /**
     * Авторизация приложения и пользователя в Google для взаимодействия с таблицами
     */
    public static void initToken() throws GoogleConnectionException {
        if (credential != null) {
            return;
        }
        try {
            credential = authorize();
        } catch (IOException | GeneralSecurityException e) {
            throw new GoogleConnectionException(e);
        }
    }

    /**
     * Добавляет информацию о вакансии на первую пустую строку
     *
     * @param vac информация о вакансии
     */
    public void addVacancyOnNewLine(Vacancy vac) throws GoogleConnectionException, GoogleRequestException {
        List<String> headers = getHeaders();
        List<CellData> vacancyInfo = new ArrayList<>(Collections.nCopies(headers.size(), null));
        Map<SheetColumn, Object> columnValueMap = vac.getSheetColumnFieldDataMap();
        for (SheetColumn column : columnValueMap.keySet()) {
            if (!headers.contains(column.name())) {
                continue;
            }
            CellData cell = new CellData();
            Object fieldValue = columnValueMap.get(column);
            switch (column.type()) {
                case FORMULA:
                case STRING:
                    if (fieldValue != null) {
                        if (!(fieldValue instanceof List)) {
                            fieldValue = fieldValue.toString();
                        } else {
                            fieldValue = ((List<?>) fieldValue).stream().map(Object::toString)
                                    .collect(Collectors.joining(", "));
                        }
                    }
                    break;
                case BOOLEAN:
                    cell.setDataValidation(new DataValidationRule().setCondition(
                            new BooleanCondition().setType("BOOLEAN")));
                    break;
                default: // Ничего не делаем
                    break;
            }
            vacancyInfo.set(headers.indexOf(column.name()),
                    cell.setUserEnteredValue(new ExtendedValue().set(column.type().getTypeName(), fieldValue)));
        }
        // Если были созданы столбцы пользователем, то в списке останутся null элементы,
        // которые надо заменить на пустые объекты
        while (vacancyInfo.contains(null)) {
            vacancyInfo.set(vacancyInfo.indexOf(null), new CellData());
        }
        batchAppendData(vacancyInfo);
        LOG.debug("Информация о вакансии добавлена.");
    }

    /**
     * Проверка наличия значения в заданном столбце
     *
     * @param value  значение для поиска
     * @param column номер столбца
     * @return `true`, если существует
     */
    public boolean isValueExist(String value, int column) throws GoogleConnectionException, GoogleRequestException {
        String range = String.format("%1$s:%1$s", (char) ('A' + (column - 1)));
        List<List<Object>> values = getData(range);
        if (values != null && !values.isEmpty()) {
            for (List<Object> v : values) {
                if (v.contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Создает/дополняет заголовки в таблице в соответствии с указанием аннотации {@link SheetColumn}
     */
    public void updateHeaders() throws GoogleConnectionException, GoogleRequestException {
        List<Object> newHeaders = new ArrayList<>();
        List<String> actualHeaders = getHeaders();
        // Собираем список заголовков, которые надо добавить в таблицу
        Vacancy.getSheetColumnList().forEach(column -> {
            if (!actualHeaders.contains(column.name())) {
                newHeaders.add(column.name());
            }
        });

        if (!newHeaders.isEmpty()) {
            String range = String.format("%s1", (char) ('A' + actualHeaders.size()));
            int updatedCells = appendData(Collections.singletonList(newHeaders), range);
            LOG.debug("В заголовок добавлено {} ячеек.", updatedCells);
        } else {
            LOG.debug("Заголовки актуальны.");
        }
    }

    /**
     * Получение списка актуальных заголовков в таблице
     */
    List<String> getHeaders() throws GoogleRequestException, GoogleConnectionException {
        List<List<Object>> values = getData("1:1");
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return values.get(0).stream().map(String::valueOf).collect(Collectors.toList());
    }

    /**
     * Добавляет данные на первую свободную строку таблицы
     *
     * @param values данные с форматированием для добавления их в одну строку
     */
    void batchAppendData(List<CellData> values) throws GoogleConnectionException, GoogleRequestException {
        try {
            AppendCellsRequest appendRequest = new AppendCellsRequest()
                    .setRows(Collections.singletonList(new RowData().setValues(values)))
                    .setFields("*");

            BatchUpdateSpreadsheetRequest spreadsheetRequest = new BatchUpdateSpreadsheetRequest()
                    .setRequests(Collections.singletonList(new Request().setAppendCells(appendRequest)));
            getSheets().batchUpdate(spreadsheetId, spreadsheetRequest).execute();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
    }

    /**
     * Добавляет данные на свободную строку таблицы, начиная с указанного диапазона
     *
     * @param values данные для добавления в формате списка из списков данных строк
     * @param range  диапазон в A1 нотации
     * @return количество измененных ячеек
     */
    Integer appendData(List<List<Object>> values, String range)
            throws GoogleConnectionException, GoogleRequestException {
        ValueRange body = new ValueRange().setValues(values);
        try {
            return getSheets().values()
                    .append(spreadsheetId, range, body).setValueInputOption("USER_ENTERED")
                    .execute()
                    .getUpdates().getUpdatedCells();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
    }

    /**
     * Возвращает данные из указанного диапазона
     *
     * @param range диапазон в A1 нотации
     * @return данные из указанного диапазона в формате списка из списков данных строк
     */
    List<List<Object>> getData(String range) throws GoogleConnectionException, GoogleRequestException {
        try {
            return getSheets().values()
                    .get(spreadsheetId, range)
                    .execute()
                    .getValues();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
    }

    /**
     * Создает экземпляр сервиса для доступа к таблицам
     */
    static Sheets.Spreadsheets getSheets() throws GoogleConnectionException {
        if (sheetsGateway == null) {
            final NetHttpTransport httpTransport;
            try {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                sheetsGateway = new Sheets
                        .Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                        .setApplicationName("JobCollector")
                        .build()
                        .spreadsheets();
            } catch (GeneralSecurityException | IOException e) {
                throw new GoogleConnectionException(e);
            }
        }
        return sheetsGateway;
    }

    /**
     * Создает объект авторизации {@code Credential}.
     * <p>
     * При первом запуске для указанного пользователя ({@code "user"}) создает token после успешной авторизации.
     * Авторизация происходит либо через открытую в браузере страницу со входом в аккаунт Google, либо при ручном
     * копировании/вставке URL, выводимой в консоль.
     * При последующих запусках в авторизации не нуждается, если сохранена автоматически создаваемая папка
     * {@code tokens}.
     * <p>
     * {@code client_secrets.json} - учетные данные авторизации, которые идентифицируют приложение на сервере Google
     *
     * @throws IOException Если файл {@code client_secrets.json} не может быть найден.
     */
    static Credential authorize() throws IOException, GeneralSecurityException {
        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        String credentialFilePath = System.getProperty("app.secrets", "");
        GoogleClientSecrets clientSecrets;
        try {
            InputStream in = new FileInputStream(Paths.get(credentialFilePath, "client_secrets.json").toString());
            clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
        } catch (FileNotFoundException e) {
            LOG.error("Отсутствует файл для доступа к Google API: client_secrets.json", e);
            throw e;
        }
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
