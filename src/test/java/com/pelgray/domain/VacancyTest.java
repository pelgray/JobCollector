package com.pelgray.domain;

import com.pelgray.domain.conditions.Salary;
import com.pelgray.domain.employer.Employer;
import com.pelgray.domain.location.Address;
import com.pelgray.domain.requirements.Experience;
import com.pelgray.domain.requirements.KeySkill;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VacancyTest {
    private Vacancy vacancy;
    private List<String> actualVacancyFields;

    @BeforeClass
    public void setUpVacancy() {
        actualVacancyFields = Arrays.asList(
                "identifier", "employer", "name", "salary",
                "experience", "key_skills", "address", "schedule",
                "employment", "specialization", "archived");

        vacancy = new Vacancy();
        vacancy.id = "123456789";
        vacancy.name = "TestName";
        vacancy.employer = new Employer("TestEmployer", "TestURL");
        vacancy.salary = new Salary(2000, 15000);
        vacancy.experience = new Experience("no experience");
        vacancy.key_skills = Arrays.asList(new KeySkill("skill1"), new KeySkill("skill2"),
                new KeySkill("skill3"));
        vacancy.address = new Address();
        vacancy.schedule = null;
        vacancy.employment = null;
        vacancy.specialization = new ArrayList<>(0);
        vacancy.archived = false;
    }

    @Test(description = "Корректная генерация словаря SheetColumn-значение поля объекта Vacancy")
    public void testGetSheetColumnFieldDataMap() {
        Map<SheetColumn, Object> expected = new HashMap<>();
        actualVacancyFields.forEach(s -> {
            try {
                Field field = Vacancy.class.getDeclaredField(s);
                expected.put(field.getAnnotation(SheetColumn.class), field.get(vacancy));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Assert.fail("Ошибка при обращении к полям класса " + Vacancy.class.getName(), e);
            }
        });

        Map<SheetColumn, Object> actual = vacancy.getSheetColumnFieldDataMap();

        Assert.assertFalse(actual.size() != expected.size(),
                "Некорректная генерация словаря по объекту Vacancy: был изменен состав выводимых полей в таблицу");
        Assert.assertEquals(actual, expected, "Некорректная генерация словаря по объекту Vacancy");
    }

    @Test(description = "Корректная генерация списка из SheetColumn полей класса Vacancy")
    public void testGetSheetColumnList() {
        List<SheetColumn> expected = actualVacancyFields.stream().map(f -> {
            try {
                return Vacancy.class.getDeclaredField(f).getAnnotation(SheetColumn.class);
            } catch (NoSuchFieldException e) {
                Assert.fail("Ошибка при обращении к полям класса " + Vacancy.class.getName(), e);
                return null;
            }
        }).collect(Collectors.toList());

        Assert.assertEquals(Vacancy.getSheetColumnList(), expected,
                "Некорректное составление списка аннотаций SheetColumn на полях класса Vacancy");
    }
}
