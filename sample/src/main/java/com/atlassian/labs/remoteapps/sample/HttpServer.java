package com.atlassian.labs.remoteapps.sample;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.URI;

/**
 *
 */
public class HttpServer
{

    private static String HOST_BASE_URL;
    private final Server server;
    private static String OUR_BASE_URL;

    public HttpServer(String appKey, String hostBaseUrl, String ourBaseUrl)
    {
        URI url = URI.create(ourBaseUrl);
        server = new Server(url.getPort());
        HOST_BASE_URL = hostBaseUrl;
        OUR_BASE_URL = ourBaseUrl;
        OAuthContext.init(appKey, ourBaseUrl);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new InfoServlet(appKey)),"/");
        context.addServlet(new ServletHolder(new MyAdminServlet()),"/myadmin");
        context.addServlet(new ServletHolder(new RegisterServlet(appKey, "global")),"/register");
        context.addServlet(new ServletHolder(new RegisterServlet(appKey, "user")),"/user-register");

        start();
    }

    public static String getHostBaseUrl()
    {
        return HOST_BASE_URL;
    }

    public static String getOurBaseUrl()
    {
        return OUR_BASE_URL;
    }

    public void start()
    {
        try
        {
            server.start();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
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

    public static void  main(String[] args)
    {
        if (args.length != 3)
        {
            System.err.println("Usage: java -jar remoteapps-sample-VERSION-standalone.jar APP_KEY, HOST_BASE_URL APP_BASE_URL");
            System.exit(1);
        }
        HttpServer server = new HttpServer(args[0], args[1], args[2]);
        server.start();
        try
        {
            server.join();
        }
        catch (InterruptedException e)
        {
            server.stop();
        }
    }

    private void join() throws InterruptedException
    {
        server.join();
    }
}
