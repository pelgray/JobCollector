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
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.exceptions.GoogleRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleSheetsUtil {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsUtil.class);
    private static final String APPLICATION_NAME = "JobCollectorTest";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    // Вставка значения по аналогии с вводом пользователя (авто приведение типа данных)
    private static final String INPUT_OPT_USER = "USER_ENTERED";

    /**
     * Добавляет текст на свободную строку, начиная с ячейки A1
     *
     * @param txt           добавляемый текст
     * @param spreadsheetId Id Google таблицы
     */
    public static void addTxtOnNewLine(String txt, String spreadsheetId)
            throws GoogleConnectionException, GoogleRequestException {
        final String range = "A1";
        Sheets service = getSheets();
        List<List<Object>> values = Collections.singletonList(Collections.singletonList(txt));
        ValueRange body = new ValueRange().setValues(values);
        AppendValuesResponse result;
        try {
            result = service.spreadsheets().values().append(spreadsheetId, range, body)
                    .setValueInputOption(INPUT_OPT_USER)
                    .execute();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
        LOG.info("{} ячеек добавлено.", result.getUpdates().getUpdatedCells());
    }

    /**
     * Добавляет информацию о вакансии на первую пустую строку
     *
     * @param vac           информация о вакансии
     * @param spreadsheetId Id Google таблицы
     * @throws ReflectiveOperationException возможна при обращении к полям класса Vacancy
     */
    public static void addVacancyOnNewLine(Vacancy vac, String spreadsheetId)
            throws GoogleConnectionException, GoogleRequestException, ReflectiveOperationException {
        List<String> fieldsOrder = createHeaderGetFieldsOrder(spreadsheetId);
        List<Object> vacancyInfo = new ArrayList<>(fieldsOrder.size());
        Object fieldValue;
        String tmp;
        for (String fieldName : fieldsOrder) {
            try {
                fieldValue = Vacancy.class.getDeclaredField(fieldName).get(vac);
                tmp = "-";
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    if (!(fieldValue instanceof List)) {
                        tmp = fieldValue.toString();
                    } else if (!((List<?>) fieldValue).isEmpty()) {
                        tmp = fieldValue.toString();
                        tmp = tmp.substring(1, tmp.length() - 1);
                    }
                }
                vacancyInfo.add(tmp);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ReflectiveOperationException("Ошибка при обращении к полям класса Vacancy", e);
            }
        }

        String range = "A1";
        Sheets service = getSheets();
        List<List<Object>> values = Collections.singletonList(vacancyInfo);
        ValueRange body = new ValueRange().setValues(values);
        AppendValuesResponse result;
        try {
            result = service.spreadsheets().values().append(spreadsheetId, range, body)
                    .setValueInputOption(INPUT_OPT_USER)
                    .execute();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
        LOG.info("{} ячеек о вакансии добавлено.", result.getUpdates().getUpdatedCells());
    }

    public static boolean existsValue(String value, int column, String spreadsheetId) throws GoogleConnectionException, GoogleRequestException {
        Sheets service = getSheets();
        String range = String.format("%1$s:%1$s", (char) ('A' + (column - 1)));
        ValueRange response;
        try {
            response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
        List<List<Object>> values = response.getValues();
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
     * Создает/дополняет заголовки в таблице в соответствии с указанием аннотации SheetColumn
     *
     * @param spreadsheetId Id Google таблицы
     * @return список полей класса Vacancy в порядке, в котором они соответствуют заголовкам в таблице
     */
    public static List<String> createHeaderGetFieldsOrder(String spreadsheetId)
            throws GoogleConnectionException, GoogleRequestException {
        List<String> fieldsOrder = new ArrayList<>();
        Sheets service = getSheets();
        // Получаем все указанные в таблице заголовки
        List<Object> headers = new ArrayList<>();
        ValueRange response;
        try {
            response = service.spreadsheets().values()
                    .get(spreadsheetId, "1:1")
                    .execute();
        } catch (IOException e) {
            throw new GoogleRequestException(e);
        }
        List<List<Object>> values = response.getValues();
        if (values != null && !values.isEmpty()) {
            headers = values.get(0);
            fieldsOrder.addAll(headers.stream().flatMap(h -> Stream.of(String.valueOf(h))).collect(Collectors.toList()));
        }
        String range = String.format("%s1", (char) ('A' + headers.size()));
        // Собираем через аннотации заголовки, которые должны быть в таблице
        Field[] declaredFields = Vacancy.class.getDeclaredFields();
        SheetColumn sheetColumn;
        for (Field field : declaredFields) {
            if (!field.isAnnotationPresent(SheetColumn.class))
                continue;
            sheetColumn = field.getAnnotation(SheetColumn.class);
            if (!headers.remove(sheetColumn.name())) {
                headers.add(sheetColumn.name());
                fieldsOrder.add(field.getName());
            } else {
                fieldsOrder.set(fieldsOrder.indexOf(sheetColumn.name()), field.getName());
            }
        }

        // К этому моменту в списке лежат только заголовки, которые надо добавить
        if (!headers.isEmpty()) {
            values = Collections.singletonList(headers);
            ValueRange body = new ValueRange().setValues(values);
            AppendValuesResponse result;
            try {
                result = service.spreadsheets().values().append(spreadsheetId, range, body)
                        .setValueInputOption(INPUT_OPT_USER)
                        .execute();
            } catch (IOException e) {
                throw new GoogleRequestException(e);
            }
            LOG.info("В заголовок добавлено {} ячеек.", result.getUpdates().getUpdatedCells());
        } else {
            LOG.info("Заголовки актуальны.");
        }
        return fieldsOrder;
    }

    /**
     * Создает экземпляр сервиса для доступа к таблицам
     */
    private static Sheets getSheets() throws GoogleConnectionException {
        final NetHttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
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
        InputStream in = GoogleSheetsUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Ресурс не найден: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
