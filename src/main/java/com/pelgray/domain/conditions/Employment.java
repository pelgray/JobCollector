package com.pelgray.domain.conditions;

/**
 * Тип занятости
 */
public class Employment {
    /**
     * Идентификатор типа занятости
     */
    String id;
    /**
     * Название типа занятости
     */
    String name;

    @Override
    public String toString() {
        return name;
    }
}
