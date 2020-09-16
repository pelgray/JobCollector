package com.pelgray;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestParsingException;

import java.util.Optional;

/**
 * Класс для общения с api.hh.ru
 */
public class HhApiService {
    private static final String API_DOMAIN = "https://api.hh.ru/vacancies/";
    public static final String DOMAIN = "hh.ru";

    public static Vacancy getVacancy(String id) {
        HttpResponse<Vacancy> response = Unirest.get(String.format("%s%s", API_DOMAIN, id)).asObject(Vacancy.class);
        Optional<UnirestParsingException> ex = response.getParsingError();
        if (ex.isPresent()) {
            throw new UnirestException("Ошибка во время исполнения запроса к api.hh.ru", ex.get());
        }
        return response.getBody();
    }
}
