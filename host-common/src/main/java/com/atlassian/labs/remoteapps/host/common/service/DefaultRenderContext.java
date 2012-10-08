package com.atlassian.labs.remoteapps.host.common.service;

import com.atlassian.labs.remoteapps.api.service.RenderContext;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

public class DefaultRenderContext implements RenderContext
{
    public static final String HOST_RESOURCE_PATH = "/remoteapps";

    private final boolean devMode = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);

    private DefaultRequestContext requestContext;
    private final LocaleResolver localeResolver;
    private final I18nResolver i18nResolver;

    public DefaultRenderContext(DefaultRequestContext requestContext, LocaleResolver localeResolver, I18nResolver i18nResolver)
    {
        this.requestContext = requestContext;
        this.localeResolver = localeResolver;
        this.i18nResolver = i18nResolver;
    }

    @Override
    public String getHostContextPath()
    {
        return URI.create(getHostBaseUrl()).getPath();
    }

    @Override
    public String getHostBaseUrl()
    {
        return requestContext.getHostBaseUrl();
    }

    @Override
    public String getHostBaseResourceUrl()
    {
        return requestContext.getHostBaseUrl() + HOST_RESOURCE_PATH;
    }

    @Override
    public String getHostStylesheetUrl()
    {
        return getHostResourceUrl("all", "css");
    }

    @Override
    public String getHostScriptUrl()
    {
        return getHostResourceUrl("all", "js");
    }

    @Override
    public String getClientKey()
    {
        return requestContext.getClientKey();
    }

    @Override
    public String getUserId()
    {
        return requestContext.getUserId();
    }

    @Override
    public Locale getLocale()
    {
        return localeResolver.getLocale(requestContext.getRequest());
    }

    @Override
    public I18nResolver getI18n()
    {
        return i18nResolver;
    }

    @Override
    public Map<String, Object> toContextMap()
    {
        return ImmutableMap.<String, Object>builder()
            .put("hostContextPath", getHostContextPath())
            .put("hostBaseUrl", getHostBaseUrl())
            .put("hostBaseResourceUrl", getHostBaseResourceUrl())
            .put("hostStylesheetUrl", getHostStylesheetUrl())
            .put("hostScriptUrl", getHostScriptUrl())
            .put("userId", getUserId())
            .put("clientKey", getClientKey())
            .put("locale", getLocale())
            .put("i18n", getI18n())
            .build();
    }

    private String getHostResourceUrl(String name, String ext)
    {
        return getHostBaseResourceUrl() + "/" + name + (devMode ? "-debug" : "") + "." + ext;
    }
}
