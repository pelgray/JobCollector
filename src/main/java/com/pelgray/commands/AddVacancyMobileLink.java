package com.pelgray.commands;

import org.springframework.stereotype.Component;

@Component
public class AddVacancyMobileLink extends AddVacancyURL {
    @Override
    protected String regex() {
        return "^(Интересная вакансия){1}";
    }
}
