package com.pelgray.commands;

import com.pelgray.TestHelper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AddVacancyURLTest {
    private final AddVacancyURL command = new AddVacancyURL();

    @DataProvider
    public Object[][] acceptData() {
        return new Object[][]{
                {"vacancy/123456", false},
                {"123456", false},
                {"vacancy", false},
                {"hh.ru/vacancy/123456", false},
                {"https://spb.hh.ru/vacancy/123456", true},
                {"http://spb.hh.ru/vacancy/123456", true},
                {"https://hh.ru/vacancy/123456", true},
                {"https://hh.ru/vacancy/123456?query=abcd", true},
                {"https://abr.hh.ru/vacancy/123456?query=abcd&ans=dcba", true},
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
                {"https://spb.hh.ru/vacancy/123456"},
                {"http://spb.hh.ru/vacancy/123456"},
                {"https://hh.ru/vacancy/123456"},
                {"https://hh.ru/vacancy/123456?query=abcd"},
                {"https://abr.hh.ru/vacancy/123456?query=abcd&ans=dcba"},
        };
    }

    @Test(description = "Извлечение URL вакансии из текста сообщения", dataProvider = "vacancyURLData")
    public void testGetVacancyURL(String msg) {
        Assert.assertEquals(command.getVacancyURL(TestHelper.getMessage(msg)), msg,
                String.format("Некорректное извлечение ссылки на вакансию из текста сообщения \"%s\"", msg));
    }

    @DataProvider
    public Object[][] vacancyIdData() {
        return new Object[][]{
                {"https://spb.hh.ru/vacancy/123456", "123456"},
                {"http://spb.hh.ru/vacancy/123456", "123456"},
                {"https://hh.ru/vacancy/123456", "123456"},
                {"https://hh.ru/vacancy/123456?query=abcd", "123456"},
                {"https://abr.hh.ru/vacancy/123456?query=abcd&ans=dcba", "123456"},
        };
    }

    @Test(description = "Извлечение URL вакансии из текста сообщения",
            dataProvider = "vacancyIdData")
    public void testGetVacancyId(String msg, String expectedId) throws Exception {
        Assert.assertEquals(command.getVacancyId(TestHelper.getMessage(msg)), expectedId,
                String.format("Некорректное извлечение ID вакансии из текста сообщения \"%s\"", msg));
    }
}
