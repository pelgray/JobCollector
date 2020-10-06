package com.pelgray.exceptions;

public class DuplicateVacancyException extends Exception {
    private static final String MESSAGE = "Эта вакансия была сохранена ранее";

    public DuplicateVacancyException() {
        super(MESSAGE);
    }
}
