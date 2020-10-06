package com.pelgray.service;

import com.pelgray.exceptions.VacancyNotFoundException;
import kong.unirest.UnirestException;
import kong.unirest.UnirestParsingException;
import org.testng.annotations.Test;

public class HhApiServiceTest {

    @Test(description = "Выброс VacancyNotFoundException, когда вакансия не найдена",
            expectedExceptions = VacancyNotFoundException.class)
    public void testVacancyNotFound() throws Exception {
        HhApiService.checkResponse(null, 404);
    }

    @Test(description = "Выброс UnirestException, когда во время запроса произошла ошибка",
            expectedExceptions = UnirestException.class)
    public void testThrowUnirestException() throws Exception {
        HhApiService.checkResponse(new UnirestParsingException("Test", new Exception()), 500);
    }
}
