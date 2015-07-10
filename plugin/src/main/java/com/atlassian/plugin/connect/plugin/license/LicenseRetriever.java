package com.atlassian.plugin.connect.plugin.license;

import javax.annotation.Nonnull;

import com.atlassian.plugin.connect.plugin.api.LicenseStatus;
import com.atlassian.upm.api.license.RemotePluginLicenseService;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Retrieves a license for a given plugin
 */
@Component
public class LicenseRetriever
{
    private final RemotePluginLicenseService remotePluginLicenseService;

    @Autowired
    public LicenseRetriever(RemotePluginLicenseService remotePluginLicenseService)
    {
        this.remotePluginLicenseService = remotePluginLicenseService;
    }

    public Option<PluginLicense> getLicense(@Nonnull String pluginKey)
    {
        return remotePluginLicenseService.getRemotePluginLicense(Preconditions.checkNotNull(pluginKey, "pluginKey"));
    }

    public LicenseStatus getLicenseStatus(String pluginKey)
    {
        return getLicense(pluginKey).map(new Function<PluginLicense, LicenseStatus>()
        {
            @Override
            public LicenseStatus apply(final PluginLicense input)
            {
                return LicenseStatus.fromBoolean(input.isActive());
            }
        }).getOrElse(LicenseStatus.NONE);
    }

    public String getServiceEntitlementNumber(String pluginKey)
    {
        return getLicense(pluginKey).flatMap(new Function<PluginLicense, Option<String>>()
        {
            @Override
            public Option<String> apply(final PluginLicense input)
            {
                return input.getSupportEntitlementNumber();
            }
        }).getOrElse((String) null);
    }

}
