package com.atlassian.plugin.remotable.host.common.service;

import com.atlassian.plugin.remotable.api.service.RenderContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipe;
import com.atlassian.plugin.remotable.host.common.service.http.DefaultRequestContext;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultRenderContext implements RenderContext
{
    public static final String HOST_RESOURCE_PATH = "/remotable-plugins";

    private final boolean devMode = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);

    private DefaultRequestContext requestContext;
    private SignedRequestHandler signedRequestHandler;
    private final LocaleResolver localeResolver;
    private final I18nResolver i18nResolver;
    private final BigPipe bigPipe;

    public DefaultRenderContext(DefaultRequestContext requestContext,
                                SignedRequestHandler signedRequestHandler,
                                LocaleResolver localeResolver,
                                I18nResolver i18nResolver,
                                BigPipe bigPipe)
    {
        this.requestContext = requestContext;
        this.signedRequestHandler = signedRequestHandler;
        this.localeResolver = localeResolver;
        this.i18nResolver = i18nResolver;
        this.bigPipe = bigPipe;
    }

    @Override
    public String getLocalBaseUrl()
    {
        return signedRequestHandler.getLocalBaseUrl();
    }

    @Override
    public String getHostContextPath()
    {
        String url = getHostBaseUrl();
        return url != null ? URI.create(url).getPath() : null;
    }

    @Override
    public String getHostBaseUrl()
    {
        return requestContext.getHostBaseUrl();
    }

    @Override
    public String getHostBaseResourceUrl()
    {
        String url = getHostBaseUrl();
        return url != null ? url + HOST_RESOURCE_PATH : null;
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
    public String getBigPipeRequestId()
    {
        return bigPipe.getRequestId();
    }

    @Override
    public boolean getBigPipeActivated()
    {
        return bigPipe.isActivated();
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
        return Collections.unmodifiableMap(new HashMap<String, Object>()
        {{
            put("localBaseUrl", getLocalBaseUrl());
            put("hostContextPath", getHostContextPath());
            put("hostBaseUrl", getHostBaseUrl());
            put("hostBaseResourceUrl", getHostBaseResourceUrl());
            put("hostStylesheetUrl", getHostStylesheetUrl());
            put("hostScriptUrl", getHostScriptUrl());
            put("userId", getUserId());
            put("clientKey", getClientKey());
            put("bigPipeRequestId", getBigPipeRequestId());
            put("bigPipeActivated", getBigPipeActivated());
            put("locale", getLocale());
            put("i18n", getI18n());
        }});
    }

    private String getHostResourceUrl(String name, String ext)
    {
        String url = getHostBaseResourceUrl();
        return url != null ? url + "/" + name + (devMode ? "-debug" : "") + "." + ext : null;
    }
}
