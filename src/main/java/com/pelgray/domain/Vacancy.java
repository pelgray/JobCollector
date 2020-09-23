package com.pelgray.domain;

import com.pelgray.domain.conditions.Employment;
import com.pelgray.domain.conditions.Salary;
import com.pelgray.domain.conditions.Schedule;
import com.pelgray.domain.employer.Employer;
import com.pelgray.domain.employer.Specialization;
import com.pelgray.domain.location.Address;
import com.pelgray.domain.location.Area;
import com.pelgray.domain.requirements.Experience;
import com.pelgray.domain.requirements.KeySkill;
import com.pelgray.domain.requirements.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Vacancy {
    /**
     * Класс-идентификатор вакансии (поле со ссылкой на вакансию)
     */
    @SheetColumn(name = "Идентификатор")
    Identifier identifier = new Identifier();

    /**
     * Идентификатор вакансии
     */
    String id;

    /**
     * Название вакансии
     */
    @SheetColumn(name = "Название")
    String name;

    /**
     * Регион размещения вакансии
     */
    Area area;

    /**
     * Специализации
     */
    @SheetColumn(name = "Специализации")
    List<Specialization> specialization;

    /**
     * Ключевые навыки
     */
    @SheetColumn(name = "Ключевые навыки")
    List<KeySkill> key_skills;

    /**
     * График работы
     */
    @SheetColumn(name = "График работы")
    Schedule schedule;

    /**
     * Требуемый опыт работы
     */
    @SheetColumn(name = "Требуемый опыт")
    Experience experience;

    /**
     * Адрес вакансии
     */
    @SheetColumn(name = "Адрес")
    Address address;

    /**
     * Тип занятости
     */
    @SheetColumn(name = "Тип занятости")
    Employment employment;

    /**
     * Оклад
     */
    @SheetColumn(name = "Оклад")
    Salary salary;

    /**
     * Короткое представление работодателя
     */
    @SheetColumn(name = "Компания")
    Employer employer;

    /**
     * В архиве
     */
    @SheetColumn(name = "В архиве")
    boolean archived;

    /**
     * Информация о наличии прикрепленного тестового задании к вакансии. В случае присутствия теста - true.
     */
    boolean has_test;

    /**
     * Информация о прикрепленном тестовом задании к вакансии. В случае отсутствия теста — null.
     */
    Test test;

    /**
     * Ссылка на представление вакансии на сайте
     */
    String alternate_url;

    /**
     * Дата и время создания вакансии
     */
    String created_at;

    /**
     * Дата и время публикации вакансии
     */
    String published_at;

    /**
     * Идентификатор вакансии со ссылкой на нее
     */
    public class Identifier {
        @Override
        public String toString() {
            return String.format("=ГИПЕРССЫЛКА(\"%s\";\"%s\")", Vacancy.this.alternate_url, Vacancy.this.id);
        }
    }


    /**
     * @param fieldsOrder порядок сортировки параметров
     * @return отсортированные в заданной последовательности параметры вакансии
     * @throws ReflectiveOperationException возникает, если нужного поля не существует, либо оно недоступно
     */
    public List<Object> getFieldsDataList(List<String> fieldsOrder) throws ReflectiveOperationException {
        List<Object> result = new ArrayList<>(fieldsOrder.size());
        for (String fieldName : fieldsOrder) {
            try {
                if (fieldName.isEmpty()) { // Избегаем зануленных пользовательских полей
                    result.add("");
                    continue;
                }
                Object fieldValue = this.getClass().getDeclaredField(fieldName).get(this);
                String tmp = "-";   // прочерк, если данных нет
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    // Необходимо получить значение поля, если это не список,
                    // и список значений, если сам список не пуст
                    if (!(fieldValue instanceof List)) {
                        tmp = fieldValue.toString();
                    } else if (!((List<?>) fieldValue).isEmpty()) {
                        tmp = Stream.of(fieldValue).map(Object::toString).collect(Collectors.joining(", "));
                    }
                }
                result.add(tmp);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ReflectiveOperationException("Ошибка при обращении к полям класса Vacancy", e);
            }
        }
        return result;
    }
}
