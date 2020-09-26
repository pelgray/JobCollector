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
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleSheetsService {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsService.class);
    private static Sheets.Spreadsheets sheetsGateway;
    private static Credential credential;
    /**
     * Id Google таблицы
     */
    private final String spreadsheetId;

    public GoogleSheetsService(String spreadsheetId) throws GoogleRequestException, GoogleConnectionException {
        this.spreadsheetId = spreadsheetId;
        updateHeaders();
    }

    /**
     * Авторизация приложения и пользователя в Google для взаимодействия с таблицами
     */
    public static void initToken() throws GoogleConnectionException {
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
     * @throws ReflectiveOperationException может возникнуть при обращении к полям класса Vacancy
     */
    public void addVacancyOnNewLine(Vacancy vac)
            throws GoogleConnectionException, GoogleRequestException, ReflectiveOperationException {
        List<Object> vacancyInfo = vac.getFieldsDataList(getOrderedFields());
        int updatedCells = appendData(Collections.singletonList(vacancyInfo), "A1");
        LOG.debug("{} ячеек о вакансии добавлено.", updatedCells);
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
        List<Object> headers = new ArrayList<>();
        // Получаем все указанные в таблице поля класса Vacancy
        List<String> orderedFields = getOrderedFields();
        // С помощью аннотаций и полученного списка полей собираем список заголовков, которые надо добавить в таблицу
        for (Field field : Vacancy.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(SheetColumn.class) && !orderedFields.contains(field.getName())) {
                headers.add(field.getAnnotation(SheetColumn.class).name());
            }
        }
        if (!headers.isEmpty()) {
            String range = String.format("%s1", (char) ('A' + orderedFields.size()));
            int updatedCells = appendData(Collections.singletonList(headers), range);
            LOG.debug("В заголовок добавлено {} ячеек.", updatedCells);
        } else {
            LOG.debug("Заголовки актуальны.");
        }
    }

    /**
     * Получение списка полей, соответствующих заголовкам в порядке, указанном в таблице
     */
    private List<String> getOrderedFields() throws GoogleRequestException, GoogleConnectionException {
        // Получаем актуальные заголовки таблицы
        List<List<Object>> values = getData("1:1");
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = values.get(0).stream().map(String::valueOf).collect(Collectors.toList());
        BitSet updatedIndexes = new BitSet(result.size());
        // Через аннотации выясняем названия полей класса Vacancy, соответствующих заголовкам
        for (Field field : Vacancy.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SheetColumn.class)) {
                continue;
            }
            SheetColumn sheetColumn = field.getAnnotation(SheetColumn.class);
            if (result.contains(sheetColumn.name())) {
                int ind = result.indexOf(sheetColumn.name());
                result.set(ind, field.getName());
                updatedIndexes.set(ind);
            }
        }
        // Если количество найденных полей в Vacancy не равно указанным в таблице
        if (updatedIndexes.cardinality() != result.size()) {
            // Зануляем такие поля во избежание ошибок
            int fromIndex = updatedIndexes.nextClearBit(0);
            while (fromIndex < result.size()) {
                result.set(fromIndex, "");
                fromIndex = updatedIndexes.nextClearBit(fromIndex + 1);
            }
        }
        return result;
    }

    /**
     * Добавляет данные на свободную строку таблицы, начиная с указанного диапазона
     *
     * @param values данные для добавления в формате списка из списков данных строк
     * @param range  диапазон в A1 нотации
     * @return количество измененных ячеек
     */
    private Integer appendData(List<List<Object>> values, String range)
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
    private List<List<Object>> getData(String range) throws GoogleConnectionException, GoogleRequestException {
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
    private static Sheets.Spreadsheets getSheets() throws GoogleConnectionException {
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
     * Создает объект авторизации <code>Credential</code>.
     * <p>
     * При первом запуске для указанного пользователя (<code>"user"</code>) создает token после успешной авторизации.
     * Авторизация происходит либо через открытую в браузере страницу со входом в аккаунт Google, либо при ручном
     * копировании/вставке URL, выводимой в консоль.
     * При последующих запусках в авторизации не нуждается, если сохранена автоматически создаваемая папка
     * <code>tokens</code>.
     * <p>
     * <code>client_secrets.json</code> - учетные данные авторизации, которые идентифицируют приложение на сервере Google
     *
     * @throws IOException Если файл <code>client_secrets.json</code> не может быть найден.
     */
    private static Credential authorize() throws IOException, GeneralSecurityException {
        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        String credentialFilePath = System.getProperty("app.secrets");
        if (credentialFilePath == null) {
            credentialFilePath = "";
        }
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
