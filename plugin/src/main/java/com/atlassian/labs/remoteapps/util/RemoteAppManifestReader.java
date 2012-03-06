package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.streams.api.StreamsEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reads the manifest entries stored by remote apps
 */
public class RemoteAppManifestReader
{
    public static String getInstallerUser(Bundle bundle)
    {
        String header = (String) bundle.getHeaders().get("Remote-App");
        if (header != null)
        {
            return OsgiHeaderUtil.parseHeader(header).get("installer").get("user");
        }
        return null;
    }
}
