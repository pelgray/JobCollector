package com.pelgray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
@PropertySource("classpath:config.properties")
@ComponentScan("com.pelgray")
public class ServiceRunner {
    private final static Logger LOG = LoggerFactory.getLogger(ServiceRunner.class);

    public static void main(String[] args) throws Exception {
        ApiContextInitializer.init();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceRunner.class);
        try {
            new TelegramBotsApi().registerBot(context.getBean("telegramBot", TelegramBot.class));
        } catch (TelegramApiException e) {
            LOG.error("Исключение на этапе регистрации Telegram-бота", e);
            throw e;
        }
        LOG.info("Бот успешно запущен");
    }
}
