package com.pelgray.exceptions;

public class GoogleRequestException extends Exception {
    private static final String MESSAGE = "Ошибка выполнения запроса к сервису Google таблиц";

    public GoogleRequestException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
