package com.pelgray.service;

import com.pelgray.domain.Vacancy;
import com.pelgray.exceptions.VacancyNotFoundException;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestParsingException;

/**
 * Класс для общения с api.hh.ru
 */
public class HhApiService {
    private static final String API_DOMAIN = "https://api.hh.ru/vacancies/";
    public static final String DOMAIN = "hh.ru";

    public static Vacancy getVacancy(String id) throws Exception {
        HttpResponse<Vacancy> response = Unirest.get(String.format("%s%s", API_DOMAIN, id)).asObject(Vacancy.class);
        checkResponse(response.getParsingError().orElse(null), response.getStatus());
        return response.getBody();
    }

    static void checkResponse(UnirestParsingException parsingError, int status) throws Exception {
        // если код ответа из серии 200-х и ошибки нет, значит запрос прошел успешно
        if (status >= 200 && status < 300 && parsingError == null) {
            return;
        }

        switch (status) {
            case 404:
                throw new VacancyNotFoundException();
            default:
                throw new UnirestException("Ошибка во время исполнения запроса к api.hh.ru", parsingError);
        }
    }
}
