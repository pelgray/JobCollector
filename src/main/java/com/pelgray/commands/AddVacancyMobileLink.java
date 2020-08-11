package com.pelgray.commands;

import org.springframework.stereotype.Component;

@Component
public class AddVacancyMobileLink extends AddVacancyURL {
    @Override
    protected String regex() {
        return "^(?:Интересная вакансия \".+\" — )(https?:\\/\\/hh\\.ru\\/vacancy\\/\\d+\\?from=share_android)" +
                "(?:\\nОтправлено с помощью мобильного приложения hh https:\\/\\/hh\\.ru\\/mobile\\?from=share_android)$";
    }
}
