package com.pelgray.commands;

import com.pelgray.GoogleSheetsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class AddVacancyURL implements ICommandHandler {
    private final static Logger LOG = LoggerFactory.getLogger(AddVacancyURL.class);
    protected final String DOMAIN = "hh.ru";
    @Value("${gSheets.SpreadsheetId}")
    private String spreadsheetId;

    @Override
    public SendMessage handle(Message msg) {
        SendMessage sm = new SendMessage(msg.getChatId(), "Добавлено").setReplyToMessageId(msg.getMessageId());
        try {
            GoogleSheetsUtil.addTxtOnNewLine(getVacancyURL(msg), spreadsheetId);
        } catch (IOException | GeneralSecurityException ex) {
            LOG.error("Неудачная попытка добавления вакансии для пользователя " + msg.getFrom().getUserName(), ex);
            return sm.setText("Не добавлено");
        }
        LOG.info("Добавлена вакансия для пользователя {}", msg.getFrom().getUserName());
        return sm;
    }

    @Override
    public boolean accept(Message msg) {
        return msg.getText().trim().matches(String.format("^%s$", regex()));
    }

    protected String regex() {
        return "https?:\\/\\/(?:\\w+\\.)?\\Q" + DOMAIN + "\\E\\/vacancy\\/\\d+(\\?.+)*";
    }

    protected String getVacancyURL(Message msg) {
        return msg.getText();
    }
}
