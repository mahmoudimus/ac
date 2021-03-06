package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.html.encode.JavascriptEncoder;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.sal.api.timezone.TimeZoneManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.Maps;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.web.iframe.EncodingUtils.escapeQuotes;
import static com.google.common.base.Strings.nullToEmpty;

public class IFrameRenderContextBuilderImpl implements IFrameRenderContextBuilder, IFrameRenderContextBuilder.AddonContextBuilder, IFrameRenderContextBuilder.NamespacedContextBuilder {

    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final TimeZoneManager timeZoneManager;

    private String addonKey;
    private String namespace;

    public IFrameRenderContextBuilderImpl(final RemotablePluginAccessorFactory pluginAccessorFactory,
                                          final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
                                          TimeZoneManager timeZoneManager) {
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.timeZoneManager = timeZoneManager;
    }

    @Override
    public AddonContextBuilder addon(final String key) {
        addonKey = key;
        return this;
    }

    @Override
    public NamespacedContextBuilder namespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Override
    public InitializedBuilder iframeUri(final String uri) {
        return new InitializedBuilderImpl(addonKey, namespace, uri);
    }

    private class InitializedBuilderImpl implements InitializedBuilder {
        private final String addonKey;
        private final String namespace;
        private final String iframeUri;

        private final Map<String, Object> additionalContext = Maps.newHashMap();

        private InitializedBuilderImpl(final String addonKey, final String namespace, final String iframeUri) {
            this.addonKey = addonKey;
            this.namespace = namespace;
            this.iframeUri = iframeUri;
        }

        private void putIfNotNull(String key, Object value) {
            if (value != null) {
                additionalContext.put(key, value);
            }
        }

        @Override
        public InitializedBuilder dialog(final boolean isDialog) {
            additionalContext.put("dialog", isDialog ? "1" : "");
            return this;
        }

        @Override
        public InitializedBuilder simpleDialog(final boolean isSimpleDialog) {
            additionalContext.put("simpleDialog", isSimpleDialog ? "1" : "");
            return this;
        }

        @Override
        public InitializedBuilder decorator(final String decorator) {
            putIfNotNull("decorator", decorator);
            return this;
        }

        @Override
        public InitializedBuilder title(final String title) {
            putIfNotNull("title", title);
            return this;
        }

        @Override
        public InitializedBuilder resizeToParent(boolean resizeToParent) {
            additionalContext.put("general", resizeToParent ? "1" : "");
            return this;
        }

        @Override
        public <T extends Map<String, String>> InitializedBuilder productContext(T productContext) {
            String json = new JSONObject(productContext).toString();
            StringWriter writer = new StringWriter();
            try {
                JavascriptEncoder.escape(writer, json);
            } catch (IOException e) {
                // there's no I/O, so there shouldn't be an IOException
                throw new IllegalStateException(e);
            }
            additionalContext.put("productContextHtml", writer.toString());
            return this;
        }

        @Override
        public InitializedBuilder context(final String key, final Object value) {
            putIfNotNull(key, value);
            return this;
        }

        @Override
        public InitializedBuilder context(final Map<String, Object> additionalContext) {
            if (additionalContext != null) {
                this.additionalContext.putAll(additionalContext);
            }
            return this;
        }

        @Override
        public Map<String, Object> build() {
            Map<String, Object> renderContext = createDefaultRenderContextParameters();
            renderContext.putAll(additionalContext);
            return renderContext;
        }

        /**
         * Add render context common to all remote iframes.
         */
        private Map<String, Object> createDefaultRenderContextParameters() {
            Map<String, Object> defaultContext = Maps.newHashMap();

            RemotablePluginAccessor plugin = pluginAccessorFactory.get(addonKey);
            UserProfile profile = userManager.getRemoteUser();
            String username = nullToEmpty(profile == null ? "" : profile.getUsername());
            String userKey = nullToEmpty(profile == null ? "" : profile.getUserKey().getStringValue());
            String timeZone = timeZoneManager.getUserTimeZone().getID();

            defaultContext.put("iframeSrcHtml", escapeQuotes(iframeUri));
            defaultContext.put("plugin", plugin);
            defaultContext.put("namespace", namespace);
            defaultContext.put("contextPath", hostApplicationInfo.getContextPath());
            defaultContext.put("userId", username);
            defaultContext.put("userKey", userKey);
            defaultContext.put("timeZone", timeZone);

            // origin is required by XDM to establish connection with iframe.
            // Since for some places iframe requires to be redirected,
            // in that case the origin can not obtain from the url because it points to the redirect servlet.
            // addOn baseUrl may contains a path but we are interested only in url with protocol and domain.
            defaultContext.put("origin", getAddOnOrigin(plugin));

            return defaultContext;
        }

        private String getAddOnOrigin(RemotablePluginAccessor plugin) {
            URI baseUrl = plugin.getBaseUrl();
            try {
                return new URI(baseUrl.getScheme(), baseUrl.getUserInfo(), baseUrl.getHost(), baseUrl.getPort(), null, null, null).toString().toLowerCase();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
