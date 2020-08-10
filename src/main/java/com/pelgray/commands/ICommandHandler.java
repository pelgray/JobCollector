package com.pelgray.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface ICommandHandler {
    SendMessage run(Message msg);

    boolean accept(Message msg);
}
