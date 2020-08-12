package com.pelgray.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class DefaultHandler implements ICommandHandler {
    private final static Logger LOG = LoggerFactory.getLogger(DefaultHandler.class);

    @Override
    public SendMessage handle(Message msg) {
        LOG.info("Сгенерировано сообщение по умолчанию для пользователя {}", msg.getFrom().getUserName());
        return new SendMessage(msg.getChatId(), "Не понял").setReplyToMessageId(msg.getMessageId());
    }

    /**
     * @return false, поскольку это класс ответа по умолчанию, в котором нет обработки команды
     */
    @Override
    public boolean accept(Message msg) {
        return false;
    }
}
