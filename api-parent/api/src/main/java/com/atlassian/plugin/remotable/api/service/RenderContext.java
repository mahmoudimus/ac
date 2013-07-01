package com.atlassian.plugin.remotable.api.service;

import com.atlassian.sal.api.message.I18nResolver;

import java.util.Locale;
import java.util.Map;

/**
 * Context information to be used during a page render
 */
public interface RenderContext
{
    String getLocalBaseUrl();

    String getHostContextPath();

    String getHostBaseUrl();

    String getHostBaseResourceUrl();

    String getHostStylesheetUrl();

    String getHostScriptUrl();

    String getClientKey();

    String getAuthState();

    String getBigPipeRequestId();

    boolean getBigPipeActivated();

    String getUserId();

    Locale getLocale();

    I18nResolver getI18n();

    /**
     * @return this context in map form
     */
    Map<String, Object> toContextMap();
}
