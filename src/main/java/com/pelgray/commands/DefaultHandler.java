package com.pelgray.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class DefaultHandler implements ICommandHandler {
    @Override
    public SendMessage handle(Message msg) {
        return new SendMessage(msg.getChatId(), "Не понял").setReplyToMessageId(msg.getMessageId());
    }

    // Возвращаем false, поскольку это класс ответа по умолчанию, в котором нет обработки команды
    @Override
    public boolean accept(Message msg) {
        return false;
    }
}