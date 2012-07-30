package com.atlassian.labs.remoteapps.kit.js;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.PolygotRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.kit.js.ringojs.RingoEngine;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.osgi.framework.BundleContext;
import org.ringojs.jsgi.JsgiServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 */
public class RingoJsKitBootstrap
{
    private static final Logger log = LoggerFactory.getLogger(RingoJsKitBootstrap.class);

    public RingoJsKitBootstrap(
            BundleContext bundleContext,
            PluginRetrievalService pluginRetrievalService,
            DescriptorGenerator descriptorGenerator) throws Exception
    {
        log.info("Starting app '" + bundleContext.getBundle().getSymbolicName() + "'");

        RingoEngine ringoEngine = new RingoEngine(pluginRetrievalService.getPlugin(), bundleContext);
        JsgiServlet servlet = new JsgiServlet(ringoEngine.getEngine());

        RemoteAppDescriptorAccessor descriptorAccessor = getDescriptorAccessor();

        // this is different than servlet kit because of how we mount a single handler on /
        descriptorGenerator.mountStaticResources("/", "/public/*");

        descriptorGenerator.mountServlet(servlet, "/");

        descriptorGenerator.init(descriptorAccessor);
    }

    private RemoteAppDescriptorAccessor getDescriptorAccessor()
    {
        File baseDir = new File(System.getProperty("plugin.resource.directories"));
        return new PolygotRemoteAppDescriptorAccessor(
                baseDir);
    }
}
