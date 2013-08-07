package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;

import org.osgi.framework.Bundle;

/**
 * Reads the manifest entries stored by remotable plugins
 */
public class RemotablePluginManifestReader
{
    public static String getInstallerUser(Bundle bundle)
    {
        String header = (String) bundle.getHeaders().get("Remote-Plugin");
        if (header != null)
        {
            return OsgiHeaderUtil.parseHeader(header).get("installer").get("user");
        }
        return null;
    }

    public static String getRegistrationUrl(Bundle bundle)
    {
        String header = (String) bundle.getHeaders().get("Remote-Plugin");
        if (header != null)
        {
            return OsgiHeaderUtil.parseHeader(header).get("installer").get("registration-url");
        }
        return null;
    }

    public static boolean isRemotePlugin(Bundle bundle)
    {
        return (bundle.getHeaders() != null && (bundle.getHeaders().get("Remote-Plugin") != null));
    }
}
