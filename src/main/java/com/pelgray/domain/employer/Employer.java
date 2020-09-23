package com.pelgray.domain.employer;

/**
 * Короткое представление работодателя
 */
public class Employer {
    /**
     * Название работодателя
     */
    String name;
    /**
     * URL для получения полного описания работодателя
     */
    String url;

    @Override
    public String toString() {
        return String.format("=ГИПЕРССЫЛКА(\"%s\";\"%s\")", url, name);
    }
}
