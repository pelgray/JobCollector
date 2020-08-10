package com.pelgray.commands;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class AddVacancyURL implements ICommandHandler {
    @Override
    public SendMessage run(Message msg) {
        SendMessage result = new SendMessage();
        result.setChatId(msg.getChatId().toString());
        result.setReplyToMessageId(msg.getMessageId());
        result.setText("Добавлено");
        return result;
    }

    @Override
    public boolean accept(Message msg) {
        String txt = msg.getText().trim();
        return txt.matches(regex());
    }

    protected String regex() {
        return "^(https?:\\/\\/)([\\w\\.\\/\\d]+)$";
    }
}
