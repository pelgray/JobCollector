package com.pelgray.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface CommandHandler {
    SendMessage handle(Message msg);

    boolean accept(Message msg);

    default SendMessage.SendMessageBuilder<?, ?> getSendMessageBuilder(Message msg) {
        return SendMessage.builder()
                .chatId(Long.toString(msg.getChatId()))
                .replyToMessageId(msg.getMessageId());
    }

}
