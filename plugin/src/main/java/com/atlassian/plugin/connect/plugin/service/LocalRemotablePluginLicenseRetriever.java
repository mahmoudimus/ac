package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.api.service.license.RemotablePluginLicense;
import com.atlassian.plugin.connect.api.service.license.RemotablePluginLicenseRetriever;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.rest.license.LicenseDetailsFactory;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Function;

import org.springframework.beans.factory.annotation.Autowired;

public class LocalRemotablePluginLicenseRetriever implements RemotablePluginLicenseRetriever
{
    private final LicenseRetriever licenseRetriever;
    private final String pluginKey;

    @Autowired
    public LocalRemotablePluginLicenseRetriever(final LicenseRetriever licenseRetriever, final String pluginKey)
    {
        this.licenseRetriever = licenseRetriever;
        this.pluginKey = pluginKey;
    }

    @Override
    public Promise<RemotablePluginLicense> retrieve()
    {
        return Promises.promise(licenseRetriever.getLicense(pluginKey).map(new Function<PluginLicense, RemotablePluginLicense>()
        {
            public RemotablePluginLicense apply(final PluginLicense input)
            {
                return LicenseDetailsFactory.createRemotablePluginLicense(input);
            }
        }).getOrElse((RemotablePluginLicense)null));
    }
}
