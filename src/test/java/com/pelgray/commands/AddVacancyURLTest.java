package com.pelgray.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class AddVacancyURLTest extends Assert {
    protected AddVacancyURL command;

    @BeforeClass
    public void setUpCommand() {
        command = new AddVacancyURL();
    }

    Message getMessage(String txt) throws IllegalAccessException, NoSuchFieldException {
        Message result = new Message();
        Field textField = Message.class.getDeclaredField("text");
        textField.setAccessible(true);
        textField.set(result, txt);
        return result;
    }

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

    /**
     * Проверка покрытия регулярным выражением URL вакансий с сайта hh.ru
     */
    @Test(dataProvider = "acceptData")
    public void testAccept(String msg, boolean expected) throws NoSuchFieldException, IllegalAccessException {
        assertEquals(command.accept(getMessage(msg)), expected,
                String.format("RegEx%s должен покрывать случай \"%s\"", expected ? "" : " не", msg));
    }

    @DataProvider
    public Object[][] vacancyURLData() {
        return new Object[][]{
                {"https://spb.hh.ru/vacancy/123456", null},
                {"http://spb.hh.ru/vacancy/123456", null},
                {"https://hh.ru/vacancy/123456", null},
                {"https://hh.ru/vacancy/123456?query=abcd", null},
                {"https://abr.hh.ru/vacancy/123456?query=abcd&ans=dcba", null},
        };
    }

    @Test(dataProvider = "vacancyURLData")
    public void testGetVacancyURL(String msg, String url) throws NoSuchFieldException, IllegalAccessException {
        assertEquals(command.getVacancyURL(getMessage(msg)), msg,
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

    @Test(dataProvider = "vacancyIdData")
    public void testGetVacancyId(String msg, String expectedId) throws Exception {
        assertEquals(command.getVacancyId(getMessage(msg)), expectedId,
                String.format("Некорректное извлечение ID вакансии из текста сообщения \"%s\"", msg));
    }
}
