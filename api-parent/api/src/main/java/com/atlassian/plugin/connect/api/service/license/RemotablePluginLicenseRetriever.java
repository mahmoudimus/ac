package com.atlassian.plugin.connect.api.service.license;

import com.atlassian.util.concurrent.Promise;

/**
 * Retrieve the license for this plugin
 *
 * @since 0.6.8
 */
public interface RemotablePluginLicenseRetriever
{
    /**
     * @return The license for the current plugin
     */
    Promise<RemotablePluginLicense> retrieve();
}
