package com.pelgray;

import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.service.GoogleSheetsService;
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
@PropertySource("file:/${app.props:${user.dir}}/config.properties")
@ComponentScan("com.pelgray")
public class ServiceRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRunner.class);

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        ApiContextInitializer.init();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ServiceRunner.class);
        TelegramBotService tgBotService = context.getBean("telegramBotService", TelegramBotService.class);
        if (tgBotService.parametersIsEmpty()) {
            LOG.error("В файле \"config.properties\" одно или несколько параметров с префиксом \"tgBot\" пусты." +
                    "\nСервис не может быть запущен.");
            return;
        }
        try {
            GoogleSheetsService.initToken();
            new TelegramBotsApi().registerBot(tgBotService);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка при подключении к Telegram боту", e);
            throw e;
        } catch (GoogleConnectionException e) {
            LOG.error("Ошибка при подключении к Google API", e);
            throw e;
        }
        LOG.info("Сервис успешно запущен за {} с", (System.currentTimeMillis() - start) / 1000);
    }
}
