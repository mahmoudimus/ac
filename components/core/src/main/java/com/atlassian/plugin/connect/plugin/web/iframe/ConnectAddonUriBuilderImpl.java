package com.atlassian.plugin.connect.plugin.web.iframe;

import java.net.URI;
import java.util.Optional;

import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.ConnectAddonUriBuilder;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.plugin.connect.spi.UserPreferencesRetriever;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 */
public class ConnectAddonUriBuilderImpl
        implements ConnectAddonUriBuilder, ConnectAddonUriBuilder.AddonUriBuilder, ConnectAddonUriBuilder.NamespacedUriBuilder, ConnectAddonUriBuilder.TemplatedBuilder
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final UserPreferencesRetriever userPreferencesRetriever;
    private PluginRetrievalService pluginRetrievalService;

    private String addonKey;
    private String namespace;
    private String templateUri;

    public ConnectAddonUriBuilderImpl(UrlVariableSubstitutor urlVariableSubstitutor,
            RemotablePluginAccessorFactory pluginAccessorFactory,
            UserManager userManager,
            HostApplicationInfo hostApplicationInfo,
            LicenseRetriever licenseRetriever,
            LocaleHelper localeHelper,
            UserPreferencesRetriever userPreferencesRetriever,
            PluginRetrievalService pluginRetrievalService)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.userPreferencesRetriever = userPreferencesRetriever;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    @Override
    public AddonUriBuilder addon(final String key)
    {
        addonKey = Preconditions.checkNotNull(key);
        return this;
    }

    @Override
    public NamespacedUriBuilder namespace(final String namespace)
    {
        this.namespace = Preconditions.checkNotNull(namespace);
        return this;
    }

    @Override
    public TemplatedBuilder urlTemplate(final String uri)
    {
        templateUri = Preconditions.checkNotNull(uri);
        return this;
    }

    @Override
    public InitializedBuilder context(final ModuleContextParameters context)
    {
        String substitutedUrl = urlVariableSubstitutor.replace(templateUri, context);
        UriBuilder uriBuilder = new UriBuilder(Uri.parse(substitutedUrl));
        return new InitializedBuilderImpl(addonKey, namespace, uriBuilder);
    }

    private class InitializedBuilderImpl implements InitializedBuilder
    {
        private final String addonKey;
        private final String namespace;
        private final UriBuilder uriBuilder;

        private boolean sign = true;
        private boolean includeStandardParams = true;
        private Optional<String> uiParameters = Optional.empty();

        private InitializedBuilderImpl(final String addonKey, final String namespace, final UriBuilder uriBuilder)
        {
            this.addonKey = addonKey;
            this.namespace = namespace;
            this.uriBuilder = uriBuilder;
        }

        @Override
        public InitializedBuilder param(final String key, final String value)
        {
            uriBuilder.addQueryParameter(key, value);
            return this;
        }

        @Override
        public InitializedBuilder dialog(boolean isDialog)
        {
            if (isDialog)
            {
                uriBuilder.addQueryParameter("dialog", "1");
                uriBuilder.addQueryParameter("simpleDialog", "1"); // TODO(chrisw): Do we still need this on the client?
            }
            return this;
        }

        @Override
        public InitializedBuilder sign(final boolean sign)
        {
            this.sign = sign;
            return this;
        }

        @Override
        public InitializedBuilder includeStandardParams(final boolean includeStandardParams)
        {
            this.includeStandardParams = includeStandardParams;
            return this;
        }

        @Override
        public InitializedBuilder uiParams(Optional<String> uiParameters)
        {
            this.uiParameters = uiParameters;
            return this;
        }

        @Override
        public String build()
        {
            if (includeStandardParams)
            {
                addStandardIFrameUrlParameters();
            }

            if(uiParameters.isPresent()) {
                uriBuilder.addQueryParameter("ui-params", uiParameters.get());
            }

            if (sign)
            {
                URI uri = uriBuilder.toUri().toJavaUri();
                return pluginAccessorFactory.getOrThrow(addonKey).signGetUrl(uri, ImmutableMap.<String, String[]>of());
            }
            else
            {
                return uriBuilder.toUri().toString();
            }
        }

        /**
         * Append query parameters common to all remote iframes.
         */
        private void addStandardIFrameUrlParameters()
        {
            UserProfile profile = userManager.getRemoteUser();

            String username = nullToEmpty(profile == null ? "" : profile.getUsername());
            String userKey = nullToEmpty(profile == null ? "" : profile.getUserKey().getStringValue());
            String timeZone = userPreferencesRetriever.getTimeZoneFor(username).getID();

            // l10n parameters
            uriBuilder.addQueryParameter("tz", timeZone);
            uriBuilder.addQueryParameter("loc", localeHelper.getLocaleTag());

            // user parameters
            uriBuilder.addQueryParameter("user_id", username);
            uriBuilder.addQueryParameter("user_key", userKey);

            // XDM parameters
            uriBuilder.addQueryParameter("xdm_e", hostApplicationInfo.getUrl().toString());
            uriBuilder.addQueryParameter("xdm_c", "channel-" + namespace);
            uriBuilder.addQueryParameter("cp", hostApplicationInfo.getContextPath());

            // licensing parameters
            uriBuilder.addQueryParameter("lic", licenseRetriever.getLicenseStatus(addonKey).value());

            // Connect framework version
            uriBuilder.addQueryParameter("cv", pluginRetrievalService.getPlugin().getPluginInformation().getVersion());
        }
    }

}
