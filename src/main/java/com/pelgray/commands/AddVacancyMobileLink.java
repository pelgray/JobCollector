package com.pelgray.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddVacancyMobileLink extends AddVacancyURL {
    private final static Logger LOG = LoggerFactory.getLogger(AddVacancyMobileLink.class);
    @Override
    protected String regex() {
        return "^(?:Интересная вакансия \".+\" — )(https?:\\/\\/\\Q" + DOMAIN + "\\E\\/vacancy\\/\\d+\\?from=share_android)" +
                "(?:\\nОтправлено с помощью мобильного приложения hh https:\\/\\/\\Q" + DOMAIN + "\\E\\/mobile\\?from=share_android)$";
    }
}
