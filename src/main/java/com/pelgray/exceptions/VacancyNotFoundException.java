package com.pelgray.exceptions;

public class VacancyNotFoundException extends Exception {
    private static final String MESSAGE = "Вакансия не найдена";

    public VacancyNotFoundException() {
        super(MESSAGE);
    }
}
