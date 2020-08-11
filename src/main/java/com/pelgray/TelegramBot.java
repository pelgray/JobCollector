package com.pelgray;

import com.pelgray.commands.ICommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${tgBot.Name}")
    private String botUsername;
    @Value("${tgBot.Token}")
    private String botToken;
    @Autowired
    private List<ICommandHandler> commands;

    /**
     * Метод для приема сообщений
     *
     * @param update содержит сообщение от пользователя
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message msg = update.getMessage();

            Optional<ICommandHandler> handlerOpt = findCommand(msg);
            SendMessage sendMessageRequest = handlerOpt.isPresent() ?
                    handlerOpt.get().handle(msg) :
                    (new SendMessage(msg.getChatId(), "Не понял").setReplyToMessageId(msg.getMessageId()));
            try {
                execute(sendMessageRequest);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод возвращает подходящую команду по полученному сообщению
     *
     * @param message сообщение от пользователя
     * @return обработчик команды
     */
    private Optional<ICommandHandler> findCommand(Message message) {
        return commands.stream()
                .filter(command -> command.accept(message))
                .findFirst();
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     *
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}
