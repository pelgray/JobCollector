package com.pelgray.domain.conditions;

/**
 * График работы
 */
public class Schedule {
    /**
     * Идентификатор графика работы
     */
    String id;
    /**
     * Название графика работы
     */
    String name;

    @Override
    public String toString() {
        return name;
    }
}
