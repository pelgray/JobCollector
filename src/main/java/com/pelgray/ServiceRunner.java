package com.pelgray;

import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.service.GoogleSheetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class ServiceRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRunner.class);

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        new AnnotationConfigApplicationContext(ServiceRunner.class);
        try {
            GoogleSheetsService.initToken();
        } catch (GoogleConnectionException e) {
            LOG.error("Ошибка при подключении к Google API", e);
            throw e;
        }
        LOG.info("Сервис успешно запущен за {} с", (System.currentTimeMillis() - start) / 1000);
    }
}
