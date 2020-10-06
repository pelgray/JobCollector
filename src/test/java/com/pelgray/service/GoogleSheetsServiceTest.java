package com.pelgray.service;

import com.pelgray.domain.SheetColumn;
import com.pelgray.domain.Vacancy;
import com.pelgray.exceptions.GoogleConnectionException;
import com.pelgray.exceptions.GoogleRequestException;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GoogleSheetsServiceTest {
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
        // соберем имеющиеся заголовки по аннотации SheetColumn и названия соответствующих им полей
        Map<String, String> fieldColumnNames = new HashMap<>(); // fieldName, columnName
        for (Field field : Vacancy.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(SheetColumn.class)) {
                fieldColumnNames.put(field.getName(), field.getAnnotation(SheetColumn.class).name());
            }
        }
        // извлечем из набора два случайных поля (считаем, что хотя бы два поля в Vacancy будут помечены SheetColumn)
        List<String> orderedFields = new ArrayList<>(fieldColumnNames.keySet());
        List<Object> missingHeaders = Arrays.asList(
                fieldColumnNames.get(orderedFields.remove((int) (Math.random() * (orderedFields.size() - 2) + 1))),
                fieldColumnNames.get(orderedFields.remove((int) (Math.random() * (orderedFields.size() - 2) + 1)))
        );

        // Given
        GoogleSheetsService service = Mockito.spy(new GoogleSheetsService(""));

        // When
        Mockito.doReturn(orderedFields).when(service).getOrderedFields();
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
        for (Object header : appendedData.get(0)) {
            Assert.assertTrue(missingHeaders.contains(header),
                    String.format("В список добавлен заголовок %s, которого там быть не должно", header));
        }
    }

    @Test(description = "Преобразование заголовков таблицы в названия полей")
    public void testGetOrderedFields() throws GoogleRequestException, GoogleConnectionException {
        // соберем имеющиеся заголовки по аннотации SheetColumn и названия соответствующих им полей
        Map<String, String> columnFieldNames = new TreeMap<>(); // columnName, fieldName
        for (Field field : Vacancy.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(SheetColumn.class)) {
                columnFieldNames.put(field.getAnnotation(SheetColumn.class).name(), field.getName());
            }
        }
        // создадим тестовый список заголовков и ожидаемый ответ
        List<Object> testHeaders = new ArrayList<>(columnFieldNames.keySet());
        List<String> expectedResult = new ArrayList<>(columnFieldNames.values());
        int customHeadersNum = 2;
        // добавим пользовательские заголовки
        while (customHeadersNum-- > 0) {
            testHeaders.add((int) (Math.random() * (testHeaders.size() - 1)), "TestHeader" + customHeadersNum);
            expectedResult.add(testHeaders.indexOf("TestHeader" + customHeadersNum), "");
        }

        // Given
        GoogleSheetsService service = Mockito.spy(new GoogleSheetsService(""));

        // When
        Mockito.doReturn(Collections.singletonList(testHeaders)).when(service).getData(Mockito.any());

        // Then
        Assert.assertEquals(service.getOrderedFields(), expectedResult,
                "Некорректное преобразование заголовков в названия полей класса Vacancy");
    }
}
