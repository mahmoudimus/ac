package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;

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

    public static String getRegistrationUrl(Bundle bundle)
    {
        String header = (String) bundle.getHeaders().get("Remote-App");
        if (header != null)
        {
            return OsgiHeaderUtil.parseHeader(header).get("installer").get("registration-url");
        }
        return null;
    }

    public static boolean isRemoteApp(Bundle bundle)
    {
        return bundle.getHeaders().get("Remote-App") != null ||
                bundle.getEntry("atlassian-remote-app.xml") != null;
    }
}
