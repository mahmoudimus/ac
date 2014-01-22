package com.atlassian.plugin.connect.plugin.iframe.render.uri;

import com.atlassian.plugin.connect.plugin.UserPreferencesRetriever;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.module.HostApplicationInfo;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.net.URI;

import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 */
public class IFrameUriBuilderImpl
        implements IFrameUriBuilder, IFrameUriBuilder.AddOnUriBuilder, IFrameUriBuilder.ModuleUriBuilder, IFrameUriBuilder.TemplatedBuilder
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;
    private final UserManager userManager;
    private final HostApplicationInfo hostApplicationInfo;
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;
    private final UserPreferencesRetriever userPreferencesRetriever;

    private String addonKey;
    private String moduleKey;
    private String templateUri;

    public IFrameUriBuilderImpl(final UrlVariableSubstitutor urlVariableSubstitutor,
            final RemotablePluginAccessorFactory pluginAccessorFactory,
            final UserManager userManager, final HostApplicationInfo hostApplicationInfo,
            final LicenseRetriever licenseRetriever, final LocaleHelper localeHelper,
            final UserPreferencesRetriever userPreferencesRetriever)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.pluginAccessorFactory = pluginAccessorFactory;
        this.userManager = userManager;
        this.hostApplicationInfo = hostApplicationInfo;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
        this.userPreferencesRetriever = userPreferencesRetriever;
    }

    @Override
    public AddOnUriBuilder addOn(final String key)
    {
        addonKey = Preconditions.checkNotNull(key);
        return this;
    }

    @Override
    public ModuleUriBuilder module(final String key)
    {
        moduleKey = Preconditions.checkNotNull(key);
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
        return new InitializedBuilderImpl(addonKey, moduleKey, uriBuilder);
    }

    private class InitializedBuilderImpl implements InitializedBuilder
    {
        private final String addonKey;
        private final String moduleKey;
        private final UriBuilder uriBuilder;

        private InitializedBuilderImpl(final String addonKey, final String moduleKey, final UriBuilder uriBuilder)
        {
            this.addonKey = addonKey;
            this.moduleKey = moduleKey;
            this.uriBuilder = uriBuilder;
        }

        @Override
        public InitializedBuilder param(final String key, final String value)
        {
            uriBuilder.addQueryParameter(key, value);
            return this;
        }

        @Override
        public String signAndBuild()
        {
            addDefaultIFrameUrlParameters();

            URI uri = uriBuilder.toUri().toJavaUri();
            return pluginAccessorFactory.getOrThrow(addonKey).signGetUrl(uri, ImmutableMap.<String, String[]>of());
        }

        @Override
        public String buildUnsigned()
        {
            return uriBuilder.toUri().toString();
        }

        /**
         * Append query parameters common to all remote iframes.
         */
        private void addDefaultIFrameUrlParameters()
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
            uriBuilder.addQueryParameter("xdm_c", "channel-" + moduleKey);
            uriBuilder.addQueryParameter("xdm_p", "1");
            uriBuilder.addQueryParameter("cp", hostApplicationInfo.getContextPath());

            // licensing parameters
            uriBuilder.addQueryParameter("lic", licenseRetriever.getLicenseStatus(addonKey).value());
        }
    }

}
