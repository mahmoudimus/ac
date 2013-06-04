package com.atlassian.plugin.remotable.plugin.util;

import com.atlassian.sal.api.message.LocaleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 */
@Component
public class LocaleHelper
{
    private static final java.lang.String SEP = "-";

    private final LocaleResolver localeResolver;

    @Autowired
    public LocaleHelper(LocaleResolver localeResolver)
    {
        this.localeResolver = localeResolver;
    }

    public String getLocaleTag()
    {
        Locale locale = localeResolver.getLocale();
        StringBuilder buf = new StringBuilder();

        if (locale.getLanguage().length() > 0) {
            buf.append(locale.getLanguage());
        }

        if (locale.getCountry().length() > 0) {
            buf.append(SEP);
            buf.append(locale.getCountry());
        }
        return buf.toString();
    }

}
