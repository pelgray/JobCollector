package com.pelgray.exceptions;

public class VacancyNotFoundException extends Exception implements JobCollectorWarning {
    private static final String MESSAGE = "Вакансия не найдена";

    public VacancyNotFoundException() {
        super(MESSAGE);
    }
}
