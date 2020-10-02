package com.pelgray.commands;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AddVacancyMobileLinkTest extends AddVacancyURLTest {
    @Override
    @BeforeClass
    public void setUpCommand() {
        command = new AddVacancyMobileLink();
    }

    @Override
    @DataProvider
    public Object[][] acceptData() {
        return new Object[][]{
                {"vacancy/123456", false},
                {"123456", false},
                {"vacancy", false},
                {"hh.ru/vacancy/123456", false},
                {"https://spb.hh.ru/vacancy/123456", false},
                {"https://hh.ru/vacancy/123456?query=abcd", false},
                {"Интересная вакансия \"VacancyName\" — https://hh.ru/vacancy/123456?from=share_android\n" +
                        "Отправлено с помощью мобильного приложения hh https://hh.ru/mobile?from=share_android", true},
        };
    }

    @Override
    @DataProvider
    public Object[][] vacancyURLData() {
        return new Object[][]{
                {"Интересная вакансия \"VacancyName\" — https://hh.ru/vacancy/123456?from=share_android\n" +
                        "Отправлено с помощью мобильного приложения hh https://hh.ru/mobile?from=share_android",
                        "https://hh.ru/vacancy/123456?from=share_android"},
        };
    }

    @Override
    @Test(description = "Извлечение URL вакансии из текста сообщения", dataProvider = "vacancyURLData")
    public void testGetVacancyURL(String msg, String url) throws NoSuchFieldException, IllegalAccessException {
        assertEquals(command.getVacancyURL(getMessage(msg)), url,
                String.format("Некорректное извлечение ссылки на вакансию из текста сообщения \"%s\"", msg));
    }
}
