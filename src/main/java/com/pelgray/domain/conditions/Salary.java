package com.pelgray.domain.conditions;

/**
 * Оклад
 */
public class Salary {
    /**
     * Нижняя граница вилки оклада
     */
    int from;
    /**
     * Верхняя граница вилки оклада
     */
    int to;
    /**
     * Признак того что оклад указан до вычета налогов. В случае если не указано - null.
     */
    boolean gross;
    /**
     * Идентификатор валюты оклада
     */
    String currency;

    @Override
    public String toString() {
        if (from == 0 && to == 0) {
            return "Не указана";
        }
        String result = "";
        if (from != 0) {
            result += String.format("от %dк ", from / 1000);
        }
        if (to != 0) {
            result += String.format("до %dк ", to / 1000);
        }
        result += gross ? "ставка" : "на руки";
        return result;
    }
}