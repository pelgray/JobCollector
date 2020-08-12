package com.pelgray.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class AddVacancyURL implements ICommandHandler {
    private final static Logger LOG = LoggerFactory.getLogger(AddVacancyURL.class);
    protected final String DOMAIN = "hh.ru";

    @Override
    public SendMessage handle(Message msg) {
        LOG.info("Добавлена вакансия для пользователя {}", msg.getFrom().getUserName());
        return new SendMessage(msg.getChatId(), "Добавлено").setReplyToMessageId(msg.getMessageId());
    }

    @Override
    public boolean accept(Message msg) {
        return msg.getText().trim().matches(regex());
    }

    protected String regex() {
        return "^https?:\\/\\/\\Q" + DOMAIN + "\\E\\/vacancy\\/\\d+(\\?.+)*$";
    }
}
