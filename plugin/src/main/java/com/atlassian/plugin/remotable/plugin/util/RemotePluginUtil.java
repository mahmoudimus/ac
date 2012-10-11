package com.atlassian.plugin.remotable.plugin.util;

import com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader;
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
