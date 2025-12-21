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
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Vacancy {
    /**
     * Класс-идентификатор вакансии (поле со ссылкой на вакансию)
     */
    @SheetColumn(name = "Идентификатор", type = SheetColumnType.FORMULA)
    Identifier identifier = new Identifier();

    /**
     * Идентификатор вакансии
     */
    String id;

    /**
     * Короткое представление работодателя
     */
    @SheetColumn(name = "Компания", type = SheetColumnType.FORMULA)
    Employer employer;

    /**
     * Название вакансии
     */
    @SheetColumn(name = "Название", type = SheetColumnType.STRING)
    String name;

    /**
     * Оклад
     */
    @SheetColumn(name = "Оклад", type = SheetColumnType.STRING)
    Salary salary;

    /**
     * Требуемый опыт работы
     */
    @SheetColumn(name = "Требуемый опыт", type = SheetColumnType.STRING)
    Experience experience;

    /**
     * Ключевые навыки
     */
    @SheetColumn(name = "Ключевые навыки", type = SheetColumnType.STRING)
    List<KeySkill> key_skills;

    /**
     * Адрес вакансии
     */
    @SheetColumn(name = "Адрес", type = SheetColumnType.STRING)
    Address address;

    /**
     * График работы
     */
    @SheetColumn(name = "График работы", type = SheetColumnType.STRING)
    Schedule schedule;

    /**
     * Тип занятости
     */
    @SheetColumn(name = "Тип занятости", type = SheetColumnType.STRING)
    Employment employment;

    /**
     * Специализации
     */
    @SheetColumn(name = "Специализации", type = SheetColumnType.STRING)
    List<Specialization> specialization;

    /**
     * В архиве
     */
    @SheetColumn(name = "В архиве", type = SheetColumnType.BOOLEAN)
    boolean archived;

    /**
     * Регион размещения вакансии
     */
    Area area;

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
     * @return словарь, в котором ключами являются существующие аннотации {@link SheetColumn} на полях класса
     * {@link Vacancy}, а значениями - значения этих аннотированных полей
     */
    public Map<SheetColumn, Object> getSheetColumnFieldDataMap() { // TODO test
        Map<SheetColumn, Object> result = new HashMap<>();
        Arrays.stream(Vacancy.class.getDeclaredFields()).filter(field -> field.isAnnotationPresent(SheetColumn.class))
                .forEachOrdered(field -> {
                    try {
                        result.put(field.getAnnotation(SheetColumn.class), field.get(this));
                    } catch (IllegalAccessException e) {
                        LoggerFactory.getLogger(Vacancy.class).warn("Ошибка при обращении к полям класса " +
                                Vacancy.class.getName(), e);
                        result.put(field.getAnnotation(SheetColumn.class), null);
                    }
                });
        return result;
    }

    /**
     * @return список из аннотаций {@link SheetColumn} на полях класса {@link Vacancy}
     */
    public static List<SheetColumn> getSheetColumnList() { // TODO test
        return Arrays.stream(Vacancy.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(SheetColumn.class))
                .map(field -> field.getAnnotation(SheetColumn.class))
                .collect(Collectors.toList());
    }
}
