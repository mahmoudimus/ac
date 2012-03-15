package com.atlassian.labs.remoteapps.kit.servlet;


import com.atlassian.plugin.module.LegacyModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;

import static com.atlassian.labs.remoteapps.apputils.Environment.getEnvAsInt;
import static com.atlassian.labs.remoteapps.apputils.Environment.setEnv;

/**
 *
 */
public class HttpServer
{
    private final Server server;
    private final ServletContextHandler context;
    private final ApplicationContext applicationContext;
    private final ProxyServletLoader proxyServletLoader;
    private final ApplicationProperties applicationProperties;

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private final URI appBaseUrl;
    private final int appPort;

    public HttpServer(ApplicationContext applicationContext, ProxyServletLoader proxyServletLoader,
            ApplicationProperties applicationProperties)
    {
        this.proxyServletLoader = proxyServletLoader;
        this.applicationProperties = applicationProperties;
        URI url;
        // todo: determine proper host when running outside a plugin
        appPort = pickFreePort(8000);
        try
        {
            url = new URI(applicationProperties.getBaseUrl() + proxyServletLoader.getAppProxyPrefix());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        appBaseUrl = url;
        setEnv("BASE_URL", appBaseUrl.toString());

        this.applicationContext = applicationContext;
        server = new Server(appPort);

        ResourceHandler staticResourceHandler = new ResourceHandler();
        String resourceBase = getClass().getResource("/public/").toString();
        staticResourceHandler.setResourceBase(resourceBase);
        staticResourceHandler.setDirectoriesListed(true);

        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.setResourceBase(resourceBase);

        HandlerList list = new HandlerList();
        list.setHandlers(new Handler[]{staticResourceHandler, context});
        server.setHandler(list);
    }

    public void start()
    {
        for (HttpServlet servlet : (Collection<HttpServlet>)applicationContext.getBeansOfType(HttpServlet.class).values())
        {
            String path;
            AppUrl appUrl = servlet.getClass().getAnnotation(AppUrl.class);
            if (appUrl != null)
            {
                path = appUrl.value();
            }
            else 
            {
                String className = servlet.getClass().getSimpleName();
                path = "/" + String.valueOf(className.charAt(0)).toLowerCase(Locale.US) + 
                        (className.endsWith("Servlet") ? className.substring(1, className.length() - "Servlet".length()) : className.substring(1, className.length()));
            }
            log.info("Found servlet '" + path + "' class '" + servlet.getClass());
            context.addServlet(new ServletHolder(servlet), path);
            context.addServlet(new ServletHolder(servlet), path + "/*");
        }

        try
        {
            log.debug("Starting app server at " + appPort);
            server.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        proxyServletLoader.start(appPort);
    }

    public void stop()
    {
        try
        {
            server.stop();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public URI getAppBaseUrl()
    {
        return appBaseUrl;
    }

    int pickFreePort(final int requestedPort)
    {
        ServerSocket socket = null;
        try
        {
            socket = new ServerSocket(requestedPort);
            return requestedPort > 0 ? requestedPort : socket.getLocalPort();
        }
        catch (final IOException e)
        {
            // happens if the requested port is taken, so we need to pick a new one
            ServerSocket zeroSocket = null;
            try
            {
                zeroSocket = new ServerSocket(0);
                return zeroSocket.getLocalPort();
            }
            catch (final IOException ex)
            {
                throw new RuntimeException("Error opening socket", ex);
            }
            finally
            {
                closeSocket(zeroSocket);
            }
        }
        finally
        {
            closeSocket(socket);
        }
    }

    private void closeSocket(ServerSocket socket)
    {
        if (socket != null)
        {
            try
            {
                socket.close();
            }
            catch (final IOException e)
            {
                throw new RuntimeException("Error closing socket", e);
            }
        }
    }
}
