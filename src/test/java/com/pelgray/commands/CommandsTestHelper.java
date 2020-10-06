package com.pelgray.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.lang.reflect.Field;

public class CommandsTestHelper {
    private static final Logger LOG = LoggerFactory.getLogger(CommandsTestHelper.class);

    public static Message getMessage(String txt) {
        Message result = new Message();
        try {
            Field textField = Message.class.getDeclaredField("text");
            textField.setAccessible(true);
            textField.set(result, txt);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOG.warn("Не удалось назначить текст объекту " + result.getClass().getName(), e);
        }
        return result;
    }
}
