package com.atlassian.labs.remoteapps.api.service;

import com.atlassian.sal.api.message.I18nResolver;

import java.util.Locale;
import java.util.Map;

public interface RenderContext
{
    String getHostContextPath();

    String getHostBaseUrl();

    String getHostBaseResourceUrl();

    String getHostStylesheetUrl();

    String getHostScriptUrl();

    String getClientKey();

    String getUserId();

    Locale getLocale();

    I18nResolver getI18n();

    Map<String, Object> toContextMap();
}
