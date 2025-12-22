package com.pelgray.service;

import com.pelgray.commands.CommandHandler;
import com.pelgray.commands.DefaultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Service
public class TelegramBotService implements LongPollingUpdateConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(TelegramBotService.class);

    private final Executor updatesProcessorExecutor;

    @Autowired
    private List<CommandHandler> commands;

    @Autowired
    private TelegramClient client;

    public TelegramBotService(ThreadFactory telegrambotNamedThreadFactory) {
        this.updatesProcessorExecutor = Executors.newSingleThreadExecutor(telegrambotNamedThreadFactory);
    }

    /**
     * Метод для приема сообщения
     *
     * @param update содержит сообщение от пользователя
     */
    public void consume(Update update) {
        LOG.debug("Получен запрос id={}", update.getUpdateId());
        if (!update.hasMessage()) {
            LOG.warn("Не понятно, как обработать запрос: {}", update);
            return;
        }
        Message msg = update.getMessage();
        LOG.debug("Получено сообщение \"{}\" от пользователя {}", msg.getText(), msg.getFrom().getUserName());

        try {
            client.execute(handleCommand(msg));
            LOG.debug("Сообщение \"{}\" от пользователя {} обработано", msg.getText(), msg.getFrom().getUserName());
        } catch (TelegramApiException e) {
            LOG.error("Не удалось выполнить отправку ответного сообщения", e);
        }
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(update -> updatesProcessorExecutor.execute(() -> consume(update)));
    }

    /**
     * Метод возвращает ответ по полученному сообщению
     *
     * @param message сообщение от пользователя
     * @return сообщение-ответ
     */
    private SendMessage handleCommand(Message message) {
        CommandHandler handler = commands.stream()
                .filter(command -> command.accept(message))
                .findFirst().orElse(new DefaultHandler());
        return handler.handle(message);
    }
}
