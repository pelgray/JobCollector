package com.pelgray.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddVacancyMobileLink extends AddVacancyURL {
    private static final Logger LOG = LoggerFactory.getLogger(AddVacancyMobileLink.class);

    @Override
    protected String getRegex() {
        return "(?:Интересная вакансия \".+\" — )(https?:\\/\\/\\Q" + DOMAIN + "\\E\\/vacancy\\/\\d+(\\?.+)*)" +
                "(?:\\nОтправлено с помощью мобильного приложения hh https:\\/\\/\\Q" + DOMAIN + "\\E\\/mobile\\?from=share_android)";
    }

    @Override
    protected String getVacancyURL(Message msg) {
        String result = msg.getText();
        Matcher matcher = Pattern.compile(super.getRegex()).matcher(result);
        if (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }
}
