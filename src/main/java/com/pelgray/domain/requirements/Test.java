package com.pelgray.domain.requirements;

/**
 * Информация о прикрепленном тестовом задании к вакансии
 */
public class Test {
    /**
     * Обязательно ли заполнение теста для отклика
     */
    boolean required;

    @Override
    public String toString() {
        return required ? "Да" : "Нет";
    }
}
