package com.atlassian.labs.remoteapps.kit.js;

import com.atlassian.labs.remoteapps.api.DescriptorAccessor;
import com.atlassian.labs.remoteapps.api.HttpResourceMounter;
import com.atlassian.labs.remoteapps.api.PolygotDescriptorAccessor;
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
            HttpResourceMounter httpResourceMounter
    ) throws Exception
    {
        log.info("Starting app '" + bundleContext.getBundle().getSymbolicName() + "'");

        RingoEngine ringoEngine = new RingoEngine(pluginRetrievalService.getPlugin(), bundleContext);

        httpResourceMounter.mountStaticResources("", "/public/*");

        JsgiServlet servlet = new JsgiServlet(ringoEngine.getEngine());
        httpResourceMounter.mountServlet(servlet, "/");
    }
}
