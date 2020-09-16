package com.pelgray.exceptions;

public class GoogleConnectionException extends Exception {
    private static final String MESSAGE = "Ошибка подключения к сервису Google таблиц";

    public GoogleConnectionException() {
        super(MESSAGE);
    }

    public GoogleConnectionException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
