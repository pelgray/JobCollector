package com.pelgray.service;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.pelgray.domain.SheetColumn;
import com.pelgray.domain.Vacancy;
import com.pelgray.domain.employer.Employer;
import com.pelgray.domain.requirements.KeySkill;
import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.exceptions.GoogleRequestException;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleSheetsServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsServiceTest.class);

    @Test(description = "Определение наличия значения в списке. Определение адреса столбца в A1-нотации по его номеру.")
    public void testIsValueExist() throws GoogleRequestException, GoogleConnectionException {
        // Given
        GoogleSheetsService service = Mockito.spy(new GoogleSheetsService(""));
        List<List<Object>> values = Arrays.asList(Collections.singletonList("item1"),
                Collections.singletonList("item2"), Collections.singletonList("item3"));

        // When
        Mockito.doReturn(values).when(service).getData(Mockito.any());

        // Then
        String message = "Некорректное определение наличия значения в списке";
        Assert.assertTrue(service.isValueExist("item3", 2), message);
        Assert.assertFalse(service.isValueExist("item4", 4), message);

        InOrder inOrder = Mockito.inOrder(service);
        // Проверка определения адреса столбца: метод getData() вызван в нужном порядке и нужными параметрами
        inOrder.verify(service).getData("B:B");
        inOrder.verify(service).getData("D:D");
        // Количество вызовов метода getData() не больше двух
        inOrder.verifyNoMoreInteractions();
    }

    @Test(description = "Недостающие заголовки добавляются в таблицу")
    public void testUpdateHeaders() throws GoogleRequestException, GoogleConnectionException {
        // соберем имеющиеся заголовки по аннотации SheetColumn
        List<String> actualHeaders = Arrays.stream(Vacancy.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(SheetColumn.class))
                .map(field -> field.getAnnotation(SheetColumn.class).name())
                .collect(Collectors.toList());

        // извлечем из набора два случайных поля (считаем, что хотя бы два поля в Vacancy будут помечены SheetColumn)
        List<Object> missingHeaders = Arrays.asList(
                actualHeaders.remove((int) (Math.random() * (actualHeaders.size() - 2) + 1)),
                actualHeaders.remove((int) (Math.random() * (actualHeaders.size() - 2) + 1))
        );

        // Given
        GoogleSheetsService service = Mockito.spy(new GoogleSheetsService(""));

        // When
        Mockito.doReturn(actualHeaders).when(service).getHeaders();
        Mockito.doReturn(missingHeaders.size()).when(service).appendData(Mockito.any(), Mockito.any());

        service.updateHeaders();

        // Then
        ArgumentCaptor<List<List<Object>>> requestCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(service, Mockito.description("Происходит добавление неверных заголовков в таблицу"))
                .appendData(requestCaptor.capture(), Mockito.any());

        // проверяем, что в appendData() был передан всего один объект
        Assert.assertEquals(requestCaptor.getAllValues().size(), 1,
                "Метод appendData() вызван больше одного раза");

        List<List<Object>> appendedData = requestCaptor.getValue();
        // проверяем, что один элемент в списке (изменяется одна строка)
        Assert.assertEquals(appendedData.size(), 1,
                "Происходит изменение больше одной заголовочной строки");
        // проверяем, что в пришедшем списке нужное количество значений
        Assert.assertEquals(appendedData.get(0).size(), missingHeaders.size(),
                "Добавляется некорректное количество заголовков в таблицу");
        // проверяем, что в пришедшем списке присутствуют правильные значения
        Assert.assertEqualsNoOrder(appendedData.get(0).toArray(), missingHeaders.toArray());
    }

    @DataProvider
    public Object[][] addVacancyOnNewLineData() {
        Map<String, Object> fieldNameValueMap = Stream.of(new Object[][]{
                {"name", "TestName"},
                {"employer", new Employer("TestEmployer", "TestURL")},
                {"key_skills", Arrays.asList(new KeySkill("skill1"), new KeySkill("skill2"),
                        new KeySkill("skill3"))},
                {"specialization", new ArrayList<>(0)},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        Vacancy vacancy = new Vacancy();
        try {
            for (String fieldName : fieldNameValueMap.keySet()) {
                Field field = Vacancy.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(vacancy, fieldNameValueMap.get(fieldName));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LOG.warn("Не удалось инициализировать поля объекта " + Vacancy.class.getName(), e);
        }

        return new Object[][]{
                {vacancy,
                        Arrays.asList("Ключевые навыки", "Специализации"),
                        Stream.of("skill1, skill2, skill3", "")
                                .map(t -> new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(t)))
                                .collect(Collectors.toList())},
                {vacancy,
                        Arrays.asList("Польз. столбец", "Название", "Компания", "alternate_url"),
                        Stream.of(null,
                                new ExtendedValue().setStringValue("TestName"),
                                new ExtendedValue().setFormulaValue(new Employer("TestEmployer", "TestURL").toString()),
                                null)
                                .map(t -> new CellData().setUserEnteredValue(t)).collect(Collectors.toList())},
        };
    }

    @Test(description = "Правильные параметры вакансии добавляются в таблицу в соответствии с текущими заголовками",
            dataProvider = "addVacancyOnNewLineData")
    public void testAddVacancyOnNewLine(Vacancy vacancy, List<String> actualHeaders, List<CellData> expectedList)
            throws GoogleRequestException, GoogleConnectionException {
        // Given
        GoogleSheetsService service = Mockito.spy(new GoogleSheetsService(""));

        // When
        Mockito.doReturn(actualHeaders).when(service).getHeaders();
        Mockito.doNothing().when(service).batchAppendData(Mockito.any());

        service.addVacancyOnNewLine(vacancy);

        // Then
        ArgumentCaptor<List<CellData>> requestCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(service).batchAppendData(requestCaptor.capture());

        // проверяем, что в batchAppendData() был передан всего один объект
        Assert.assertEquals(requestCaptor.getAllValues().size(), 1,
                "Метод batchAppendData() вызван больше одного раза");

        List<CellData> appendedData = requestCaptor.getValue();
        // проверяем, что в пришедшем списке находятся правильные значения в правильном порядке
        Assert.assertEquals(appendedData, expectedList);
    }
}
