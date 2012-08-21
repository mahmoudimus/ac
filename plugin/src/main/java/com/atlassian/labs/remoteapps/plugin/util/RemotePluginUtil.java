package com.atlassian.labs.remoteapps.plugin.util;

import org.osgi.framework.Bundle;

/**
 *
 */
public class RemotePluginUtil
{
    public static boolean isRemoteMode(Bundle bundle)
    {
        return RemoteAppManifestReader.isRemoteApp(bundle);
    }
}
