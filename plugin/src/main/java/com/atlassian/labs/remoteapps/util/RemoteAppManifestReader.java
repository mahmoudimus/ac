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
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 11/01/12
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
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
