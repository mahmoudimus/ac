package com.atlassian.labs.remoteapps.container;


import com.atlassian.plugin.Plugin;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Set;

import static java.lang.Integer.parseInt;

/**
 *
 */
public class HttpServer
{
    private final Server server;
    private final Map<String,ServletContextHandler> contexts;
    private final MutableHandlerList handlers;

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private final int appPort;

    public HttpServer()
    {
        String port = System.getenv("PORT");
        appPort = pickFreePort(port != null ? parseInt(port) : 8000);

        server = new Server(appPort);

        handlers = new MutableHandlerList();
        server.setHandler(handlers);

        contexts = new MapMaker().makeComputingMap(new Function<String, ServletContextHandler>()
        {
            @Override
            public ServletContextHandler apply(@Nullable String appKey)
            {
                log.info("Remote app {} started at {}", appKey, getLocalMountBaseUrl(appKey));
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/" + appKey);
                Handler[] oldContexts = handlers.getHandlers() != null ? handlers.getHandlers() : new Handler[0];

                Handler[] list = new Handler[oldContexts.length + 1];
                System.arraycopy(oldContexts, 0, list, 0, oldContexts.length);
                list[oldContexts.length] = context;
                reloadHandlers(list);
                return context;
            }
        });
    }

    public void mountServlet(Plugin plugin, HttpServlet servlet, Iterable<String> paths)
    {
        ServletContextHandler context = contexts.get(plugin.getKey());
        for (String path : paths)
        {
            context.addServlet(new ServletHolder(servlet), path);
        }
        setResourceBase(context, plugin);
        restartContext(context);
    }

    private void setResourceBase(ServletContextHandler context, Plugin plugin)
    {
        if (context.getResourceBase() == null)
        {
            context.setResourceBase(plugin.getResource("/").toString());
        }
    }

    private void restartContext(ServletContextHandler context)
    {
        try
        {
            context.stop();
            context.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void mountStaticResources(Plugin plugin, String resourceBasePath)
    {
        ServletContextHandler ctx = contexts.get(plugin.getKey());
        setResourceBase(ctx, plugin);
        ctx.setInitParameter("org.eclipse.jetty.servlet.Default.relativeResourceBase", resourceBasePath);
        ctx.addServlet(new ServletHolder(new DefaultServlet()), "/");
        restartContext(ctx);

    }

    private void reloadHandlers(Handler[] list)
    {
        try
        {
            handlers.stop();
            handlers.setHandlers(list);
            handlers.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void start() throws Exception
    {
        server.start();
    }

    public String getLocalMountBaseUrl(String appKey)
    {
        return "http://localhost:" + appPort + "/" + appKey;
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

    public void join() throws InterruptedException
    {
        server.join();
    }

    public int getAppPort()
    {
        return appPort;
    }

    public Set<String> getContextNames()
    {
        return contexts.keySet();
    }
}
