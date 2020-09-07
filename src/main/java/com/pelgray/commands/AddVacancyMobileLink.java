package com.pelgray.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AddVacancyMobileLink extends AddVacancyURL {
    private final static Logger LOG = LoggerFactory.getLogger(AddVacancyMobileLink.class);

    @Override
    protected String regex() {
        return "(?:Интересная вакансия \".+\" — )(https?:\\/\\/\\Q" + DOMAIN + "\\E\\/vacancy\\/\\d+(\\?.+)*)" +
                "(?:\\nОтправлено с помощью мобильного приложения hh https:\\/\\/\\Q" + DOMAIN + "\\E\\/mobile\\?from=share_android)";
    }

    @Override
    protected String getVacancyURL(Message msg) {
        String text = msg.getText();
        Matcher matcher = Pattern.compile(super.regex()).matcher(text);
        if (matcher.find()) {
            text = matcher.group();
        }
        return text;
    }
}
