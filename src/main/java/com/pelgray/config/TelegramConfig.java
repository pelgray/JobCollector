package com.pelgray.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pelgray.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.ThreadFactory;

@Configuration
@PropertySource("file:/${app.props:${user.dir}}/config.properties")
public class TelegramConfig {

    @Bean
    TelegramClient telegramClient(@Value("${tgBot.Token}") String token) {
        return new OkHttpTelegramClient(token);
    }

    /**
     * Фабрика потоков с названием, равным имени Telegram бота. Сделано для удобства просмотра логов.
     *
     * @param botUsername название Telegram бота
     */
    @Bean
    ThreadFactory telegrambotNamedThreadFactory(@Value("${tgBot.Name}") String botUsername) {
        return new ThreadFactoryBuilder().setNameFormat("%s-thr-%%d".formatted(botUsername)).build();
    }

    @Bean
    TelegramBotsLongPollingApplication botsApplication(@Value("${tgBot.Token}") String botToken,
                                                       TelegramBotService botService) {
        TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
        try {
            app.registerBot(botToken, botService);
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при подключении к Telegram боту", ex);
        }
        return app;
    }

}
