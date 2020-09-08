package com.pelgray;

public class Vacancy {
    /**
     * Идентификатор вакансии (поле со ссылкой на вакансию)
     */
    String id;
    /**
     * Название вакансии
     */
    String name;
    /**
     * Специализации
     */
    String specialization;
    /**
     * Ключевые навыки
     */
    String[] key_skills;
    /**
     * График работы
     */
    String schedule;
    /**
     * Требуемый опыт работы
     */
    String experience;
    /**
     * Адрес вакансии
     */
    String address;
    /**
     * Тип занятости
     */
    String employment;
    /**
     * Оклад
     */
    int[] salary;
    /**
     * В архиве
     */
    boolean archived;
}
