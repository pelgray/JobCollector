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

    public Employer(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return String.format("=ГИПЕРССЫЛКА(\"%s\";\"%s\")", url, name);
    }
}
