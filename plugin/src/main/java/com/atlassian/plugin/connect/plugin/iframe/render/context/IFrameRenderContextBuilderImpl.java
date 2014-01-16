package com.atlassian.plugin.connect.plugin.iframe.render.context;

import com.atlassian.html.encode.JavascriptEncoder;
import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.module.HostApplicationInfo;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 */
public class IFrameRenderContextBuilderImpl implements IFrameRenderContextBuilder, IFrameRenderContextBuilder.AddOnContextBuilder, IFrameRenderContextBuilder.ModuleContextBuilder
{
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final UserPreferencesRetriever userPreferencesRetriever;

    private String addonKey;
    private String moduleKey;

    public IFrameRenderContextBuilderImpl(final RemotablePluginAccessorFactory pluginAccessorFactory,
                                          final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
                                          final UserPreferencesRetriever userPreferencesRetriever) {
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.userPreferencesRetriever = userPreferencesRetriever;
    }

    @Override
    public AddOnContextBuilder addOn(final String key)
    {
        addonKey = key;
        return this;
    }

    @Override
    public ModuleContextBuilder module(final String key)
    {
        moduleKey = key;
        return this;
    }

    @Override
    public InitializedBuilder iframeUri(final String uri)
    {
        return new InitializedBuilderImpl(addonKey, moduleKey, uri);
    }

    private class InitializedBuilderImpl implements InitializedBuilder
    {
        private final String addonKey;
        private final String moduleKey;
        private final String iframeUri;

        private final Map<String, Object> additionalContext = Maps.newHashMap();

        private InitializedBuilderImpl(final String addonKey, final String moduleKey, final String iframeUri) {
            this.addonKey = addonKey;
            this.moduleKey = moduleKey;
            this.iframeUri = iframeUri;
        }

        @Override
        public InitializedBuilder dialog(String dialogId)
        {
            additionalContext.put("dialog", dialogId);
            return this;
        }

        @Override
        public InitializedBuilder simpleDialog(final String simpleDialogId)
        {
            additionalContext.put("simpleDialog", simpleDialogId);
            return this;
        }

        @Override
        public InitializedBuilder decorator(final String decorator)
        {
            additionalContext.put("decorator", decorator);
            return this;
        }

        @Override
        public InitializedBuilder title(final String title)
        {
            additionalContext.put("title", title);
            return this;
        }

        @Override
        public InitializedBuilder productContext(final Map<String, Object> productContext)
        {
            String json = new JSONObject(productContext).toString();
            StringWriter writer = new StringWriter();
            try
            {
                JavascriptEncoder.escape(writer, json);
            }
            catch (IOException e)
            {
                // there's no I/O, so there shouldn't be an IOException
                throw new IllegalStateException(e);
            }
            additionalContext.put("productContextHtml", writer.toString());
            return this;
        }

        @Override
        public InitializedBuilder context(final String key, final Object value)
        {
            additionalContext.put(key, value);
            return this;
        }

        @Override
        public Map<String, Object> build()
        {
            Map<String, Object> renderContext = createDefaultRenderContextParameters();
            renderContext.putAll(additionalContext);
            return renderContext;
        }

        /**
         * Add render context common to all remote iframes.
         */
        private Map<String, Object> createDefaultRenderContextParameters()
        {
            Map<String, Object> defaultContext = Maps.newHashMap();

            UserProfile profile = userManager.getRemoteUser();

            String username = nullToEmpty(profile == null ? "" : profile.getUsername());
            String userKey = nullToEmpty(profile == null ? "" : profile.getUserKey().getStringValue());
            String timeZone = userPreferencesRetriever.getTimeZoneFor(username).getID();

            defaultContext.put("iframeSrcHtml", escapeQuotes(iframeUri));
            defaultContext.put("plugin", pluginAccessorFactory.getOrThrow(addonKey));
            defaultContext.put("namespace", moduleKey);
            defaultContext.put("contextPath", hostApplicationInfo.getContextPath());
            defaultContext.put("userId", username);
            defaultContext.put("userKey", userKey);
            defaultContext.put("data", ImmutableMap.of("timeZone", timeZone));

            return defaultContext;
        }

    }
}
