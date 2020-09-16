package com.pelgray.exceptions;

public class DuplicateVacancyException extends Exception implements JobCollectorWarning {
    private static final String MESSAGE = "Эта вакансия уже сохранена";

    public DuplicateVacancyException() {
        super(MESSAGE);
    }
}
