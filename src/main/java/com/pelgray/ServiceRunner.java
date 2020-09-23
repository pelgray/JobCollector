package com.pelgray;

import com.pelgray.service.TelegramBotService;
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
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRunner.class);

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        ApiContextInitializer.init();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceRunner.class);
        try {
            new TelegramBotsApi().registerBot(context.getBean("telegramBotService", TelegramBotService.class));
        } catch (TelegramApiException e) {
            LOG.error("Ошибка при подключении к Telegram боту", e);
            throw e;
        }
        LOG.info("Сервис успешно запущен за {} с", (System.currentTimeMillis() - start) / 1000);
    }
}
