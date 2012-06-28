package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.container.HttpServer;
import com.atlassian.plugin.Plugin;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

import static java.util.Arrays.asList;

/**
 * Kicks off the descriptor generator and sends failure events
 */
public class DescriptorGeneratorLoader implements DescriptorGenerator
{
    private final HttpServer httpServer;
    private static final Logger log = LoggerFactory.getLogger(DescriptorGeneratorLoader.class);
    private final Plugin plugin;

    public DescriptorGeneratorLoader(Plugin plugin, HttpServer httpServer)
    {
        this.httpServer = httpServer;
        this.plugin = plugin;
    }

    @Override
    public String getLocalMountBaseUrl()
    {
        return httpServer.getLocalMountBaseUrl(plugin.getKey());
    }

    @Override
    public void init(Document document) throws Exception
    {
        // we do nothing with this right now.  Maybe validate or auto-install the descriptor?
    }

    @Override
    public void mountServlet(HttpServlet httpServlet, String... urlPatterns)
    {
        httpServer.mountServlet(plugin, httpServlet, asList(urlPatterns));
    }

    @Override
    public void mountStaticResources(String resourcePrefix)
    {
        httpServer.mountStaticResources(plugin, resourcePrefix);
    }
}
