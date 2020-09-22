package com.pelgray;

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
import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.exceptions.GoogleRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleSheetsService {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsService.class);
    private static final String APPLICATION_NAME = "JobCollectorTest";
    /**
     * Id Google таблицы
     */
    private final String spreadsheetId;

    public GoogleSheetsService(String spreadsheetId) throws GoogleRequestException, GoogleConnectionException {
        this.spreadsheetId = spreadsheetId;
        updateHeaders();
    }

    /**
     * Добавляет информацию о вакансии на первую пустую строку
     *
     * @param vac информация о вакансии
     * @throws ReflectiveOperationException может возникнуть при обращении к полям класса Vacancy
     */
    public void addVacancyOnNewLine(Vacancy vac)
            throws GoogleConnectionException, GoogleRequestException, ReflectiveOperationException {
        List<Object> vacancyInfo = vac.getFieldsDataList(getFieldsOrder());
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
        List<String> fieldsOrder = getFieldsOrder();
        // С помощью аннотаций и полученного списка полей собираем список заголовков, которые надо добавить в таблицу
        for (Field field : Vacancy.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SheetColumn.class) || fieldsOrder.contains(field.getName()))
                continue;
            headers.add(field.getAnnotation(SheetColumn.class).name());
        }
        if (!headers.isEmpty()) {
            String range = String.format("%s1", (char) ('A' + fieldsOrder.size()));
            int updatedCells = appendData(Collections.singletonList(headers), range);
            LOG.debug("В заголовок добавлено {} ячеек.", updatedCells);
        } else {
            LOG.debug("Заголовки актуальны.");
        }
    }

    /**
     * Получение списка полей, соответствующих заголовкам в актуальном порядке
     */
    public List<String> getFieldsOrder() throws GoogleRequestException, GoogleConnectionException {
        // Получаем актуальные заголовки таблицы
        List<List<Object>> values = getData("1:1");
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> fieldsOrder = values.get(0).stream().map(String::valueOf).collect(Collectors.toList());
        BitSet updatedIndexes = new BitSet(fieldsOrder.size());
        // Через аннотации выясняем названия полей класса Vacancy, соответствующих заголовкам
        for (Field field : Vacancy.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SheetColumn.class)) {
                continue;
            }
            SheetColumn sheetColumn = field.getAnnotation(SheetColumn.class);
            if (fieldsOrder.contains(sheetColumn.name())) {
                int ind = fieldsOrder.indexOf(sheetColumn.name());
                fieldsOrder.set(ind, field.getName());
                updatedIndexes.set(ind);
            }
        }
        // Если количество найденных полей в Vacancy не равно указанным в таблице
        if (updatedIndexes.cardinality() != fieldsOrder.size()) {
            // Зануляем такие поля во избежание ошибок
            int fromIndex = updatedIndexes.nextClearBit(0);
            while (fromIndex < fieldsOrder.size()) {
                fieldsOrder.set(fromIndex, "");
                fromIndex = updatedIndexes.nextClearBit(fromIndex + 1);
            }
        }
        return fieldsOrder;
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
        final NetHttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), getCredentials(httpTransport))
                    .setApplicationName(APPLICATION_NAME)
                    .build()
                    .spreadsheets();
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleConnectionException(e);
        }
    }

    /**
     * Создает объект авторизации Credential.
     *
     * @throws IOException Если файл credentials.json не может быть найден.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        String credentialsFilePath = "/credentials.json";
        String tokensDirectoryPath = "tokens";
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        InputStream in = GoogleSheetsService.class.getResourceAsStream(credentialsFilePath);
        if (in == null) {
            throw new FileNotFoundException("Ресурс не найден: " + credentialsFilePath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
