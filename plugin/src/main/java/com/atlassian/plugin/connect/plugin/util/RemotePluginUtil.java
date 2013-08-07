package com.atlassian.plugin.connect.plugin.util;


import org.osgi.framework.Bundle;

/**
 *
 */
public class RemotePluginUtil
{
    public static boolean isRemoteMode(Bundle bundle)
    {
        return RemotablePluginManifestReader.isRemotePlugin(bundle);
    }
}
