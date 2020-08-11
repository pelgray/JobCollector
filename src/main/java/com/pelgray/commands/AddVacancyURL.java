package com.pelgray.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class AddVacancyURL implements ICommandHandler {
    @Override
    public SendMessage handle(Message msg) {
        return new SendMessage(msg.getChatId(), "Добавлено").setReplyToMessageId(msg.getMessageId());
    }

    @Override
    public boolean accept(Message msg) {
        return msg.getText().trim().matches(regex());
    }

    protected String regex() {
        return "^https?:\\/\\/hh\\.ru\\/vacancy\\/\\d+(\\?.+)*$";
    }
}
