package com.pelgray.domain;

import com.google.api.services.sheets.v4.model.BooleanCondition;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.DataValidationRule;
import com.google.api.services.sheets.v4.model.ExtendedValue;
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
     * Метод преобразования списка названий полей класса {@link Vacancy} в список значений этих полей для добавления
     * новой строки в таблице
     * <br>
     * Если в {@code orderedFields} есть пустые значения (то есть, в таблице присутствуют столбцы, созданные
     * пользователем) или не аннотированные {@link SheetColumn} поля, то им присваивается {@code null}
     *
     * @param orderedFields список полей класса в той же последовательности, как они указаны в таблице
     * @return параметры вакансии, соответствующие полученному списку полей
     * @throws ReflectiveOperationException возникает, если нужного поля не существует, либо оно недоступно
     */
    public List<CellData> getFieldsDataList(List<String> orderedFields) throws ReflectiveOperationException {
        List<CellData> result = new ArrayList<>(orderedFields.size());
        for (String fieldName : orderedFields) {
            try {
                CellData cell = new CellData();
                // Пропускаем столбцы, созданные пользователем
                if (fieldName.isEmpty() ||
                        !this.getClass().getDeclaredField(fieldName).isAnnotationPresent(SheetColumn.class)) {
                    result.add(cell);
                    continue;
                }
                Object fieldValue = this.getClass().getDeclaredField(fieldName).get(this);
                SheetColumn sheetColumn = this.getClass().getDeclaredField(fieldName).getAnnotation(SheetColumn.class);
                switch (sheetColumn.type()) {
                    case FORMULA:
                    case STRING:
                        if (fieldValue != null) {
                            if (!(fieldValue instanceof List)) {
                                fieldValue = fieldValue.toString();
                            } else {
                                fieldValue = ((List<?>) fieldValue).stream().map(Object::toString)
                                        .collect(Collectors.joining(", "));
                            }
                        }
                        break;
                    case BOOLEAN:
                        cell.setDataValidation(new DataValidationRule().setCondition(
                                new BooleanCondition().setType("BOOLEAN")));
                        break;
                    default: // Ничего не делаем
                        break;
                }
                result.add(cell.setUserEnteredValue(new ExtendedValue().set(sheetColumn.type().getTypeName(), fieldValue)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ReflectiveOperationException("Ошибка при обращении к полям класса Vacancy", e);
            }
        }
        return result;
    }
}
