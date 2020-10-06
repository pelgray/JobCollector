package com.pelgray.commands;

import com.pelgray.TestHelper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AddVacancyMobileLinkTest {
    private final AddVacancyMobileLink command = new AddVacancyMobileLink();

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

    @Test(description = "Покрытие регулярным выражением URL вакансий с сайта hh.ru",
            dataProvider = "acceptData")
    public void testAccept(String msg, boolean expected) {
        Assert.assertEquals(command.accept(TestHelper.getMessage(msg)), expected,
                String.format("RegEx%s должен покрывать случай \"%s\"", expected ? "" : " не", msg));
    }

    @DataProvider
    public Object[][] vacancyURLData() {
        return new Object[][]{
                {"Интересная вакансия \"VacancyName\" — https://hh.ru/vacancy/123456?from=share_android\n" +
                        "Отправлено с помощью мобильного приложения hh https://hh.ru/mobile?from=share_android",
                        "https://hh.ru/vacancy/123456?from=share_android"},
        };
    }

    @Test(description = "Извлечение URL вакансии из текста сообщения", dataProvider = "vacancyURLData")
    public void testGetVacancyURL(String msg, String url) {
        Assert.assertEquals(command.getVacancyURL(TestHelper.getMessage(msg)), url,
                String.format("Некорректное извлечение ссылки на вакансию из текста сообщения \"%s\"", msg));
    }
}
