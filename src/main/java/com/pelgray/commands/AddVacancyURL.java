package com.pelgray.commands;

import com.pelgray.GoogleSheetsService;
import com.pelgray.HhApiService;
import com.pelgray.Vacancy;
import com.pelgray.exceptions.DuplicateVacancyException;
import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.exceptions.GoogleRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddVacancyURL implements ICommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AddVacancyURL.class);
    @Value("${gSheets.SpreadsheetId}")
    private String spreadsheetId;
    private GoogleSheetsService sheetsService;
    protected final String DOMAIN = HhApiService.DOMAIN;

    @Override
    public SendMessage handle(Message msg) {
        SendMessage sm = new SendMessage(msg.getChatId(), "Добавлено").setReplyToMessageId(msg.getMessageId());
        try {
            String vacancyId = getVacancyId(msg);
            if (getSheetsService().isValueExist(vacancyId, 1)) {
                throw new DuplicateVacancyException();
            }
            Vacancy vacancy = HhApiService.getVacancy(vacancyId);
            getSheetsService().addVacancyOnNewLine(vacancy);
            LOG.info("Добавлена вакансия для пользователя {}", msg.getFrom().getUserName());
            return sm;
        } catch (DuplicateVacancyException e) {
            return sm.setText("Не добавлено.\n" + e.getMessage());
        } catch (Exception e) {
            LOG.error(String.format("Неудачная попытка добавления вакансии (%s): %s",
                    msg.getFrom().getUserName(), e.getMessage()), e);
            return sm.setText("Произошла непредвиденная ошибка.\n" + e.getMessage());
        }
    }

    @Override
    public boolean accept(Message msg) {
        return msg.getText().trim().matches(String.format("^%s$", getRegex()));
    }

    protected String getRegex() {
        return "https?:\\/\\/(?:\\w+\\.)?\\Q" + DOMAIN + "\\E\\/vacancy\\/\\d+(\\?.+)*";
    }

    protected String getVacancyURL(Message msg) {
        return msg.getText();
    }

    private String getVacancyId(Message msg) throws Exception {
        String url = getVacancyURL(msg);
        Matcher matcher = Pattern.compile("\\d+").matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception(String.format("Не удалось получить идентификатор вакансии из адреса '%s'", url));
    }

    private GoogleSheetsService getSheetsService() throws GoogleRequestException, GoogleConnectionException {
        if (sheetsService == null) {
            sheetsService = new GoogleSheetsService(spreadsheetId);
        }
        return sheetsService;
    }
}
