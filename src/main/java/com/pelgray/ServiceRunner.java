package com.pelgray;

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
    public static void main(String[] args) throws Exception {
        ApiContextInitializer.init();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceRunner.class);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(context.getBean("telegramBot", TelegramBot.class));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            throw e;
        }
        System.out.println("Бот запущен");
    }
}
