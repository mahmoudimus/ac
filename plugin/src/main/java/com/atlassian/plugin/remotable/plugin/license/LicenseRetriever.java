package com.atlassian.plugin.remotable.plugin.license;

import com.atlassian.upm.api.license.RemotePluginLicenseService;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public Option<PluginLicense> getLicense(String pluginKey)
    {
        return remotePluginLicenseService.getRemotePluginLicense(pluginKey);
    }

    public LicenseStatus getLicenseStatus(String pluginKey)
    {
        return getLicense(pluginKey).map(new Function<PluginLicense, LicenseStatus>()
        {
            @Override
            public LicenseStatus apply(final PluginLicense input)
            {
                return input.isActive() ? LicenseStatus.ACTIVE : LicenseStatus.NONE;
            }
        }).getOrElse(LicenseStatus.NONE);
    }

}
