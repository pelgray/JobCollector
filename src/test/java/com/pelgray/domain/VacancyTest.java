package com.pelgray.domain;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.pelgray.domain.employer.Employer;
import com.pelgray.domain.requirements.KeySkill;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VacancyTest {
    private Vacancy vacancy;

    @BeforeClass
    public void setUpVacancy() {
        vacancy = new Vacancy();
        vacancy.name = "TestName";
        vacancy.employer = new Employer("TestEmployer", "TestURL");
        vacancy.key_skills = Arrays.asList(new KeySkill("skill1"), new KeySkill("skill2"),
                new KeySkill("skill3"));
        vacancy.specialization = new ArrayList<>(0);
    }

    @Test(description = "Получение списка значений полей объекта Vacancy")
    public void testGetFieldsDataListWithFieldsParams() throws ReflectiveOperationException {
        List<String> actualFieldNames = Arrays.asList("", "name", "employer", "alternate_url");

        List<ExtendedValue> expectedValues = Arrays.asList(null,
                new ExtendedValue().setStringValue(vacancy.name),
                new ExtendedValue().setFormulaValue(vacancy.employer.toString()),
                null);
        List<CellData> expectedList = expectedValues.stream()
                .map(t -> new CellData().setUserEnteredValue(t)).collect(Collectors.toList());

        Assert.assertEquals(vacancy.getFieldsDataList(actualFieldNames), expectedList,
                "Некорректный вывод значений полей");
    }

    @Test(description = "Получение списка значений полей-списков объекта Vacancy")
    public void testGetFieldsDataListWithListParams() throws ReflectiveOperationException {
        List<String> actualFieldNames = Arrays.asList("key_skills", "specialization");

        List<CellData> expectedList = Stream.of("skill1, skill2, skill3", "")
                .map(t -> new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(t)))
                .collect(Collectors.toList());

        Assert.assertEquals(vacancy.getFieldsDataList(actualFieldNames), expectedList,
                "Некорректный вывод значений списков");
    }

    @Test(description = "Выброс исключения при попытке обратиться к несуществующему полю класса Vacancy",
            expectedExceptions = ReflectiveOperationException.class)
    public void testExceptionGetFieldsDataList() throws ReflectiveOperationException {
        vacancy.getFieldsDataList(Collections.singletonList("nameTestABC"));
    }
}
