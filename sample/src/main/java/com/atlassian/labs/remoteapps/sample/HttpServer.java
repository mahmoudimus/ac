package com.atlassian.labs.remoteapps.sample;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
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

    public HttpServer(String appKey, String hostBaseUrl, String ourBaseUrl, int port)
    {
        server = new Server(port);
        HOST_BASE_URL = hostBaseUrl;
        OUR_BASE_URL = ourBaseUrl + ":" + port;
        OAuthContext.init(appKey, ourBaseUrl);

        ResourceHandler staticResourceHandler = new ResourceHandler();
        String resourceBase = getClass().getResource("/static/").toString();
        System.out.println("resource base: " + resourceBase);
        staticResourceHandler.setResourceBase(resourceBase);
        staticResourceHandler.setDirectoriesListed(true);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.setResourceBase(resourceBase);

        context.addServlet(new ServletHolder(new InfoServlet(appKey)), "/");
        context.addServlet(new ServletHolder(new MyAdminServlet()), "/myadmin");
        context.addServlet(new ServletHolder(new MyMacroServlet()), "/mymacro");
        context.addServlet(new ServletHolder(new MyImageMacroServlet()), "/myimagemacro");
        context.addServlet(new ServletHolder(new MySlowMacroServlet()), "/myslowmacro");
        context.addServlet(new ServletHolder(new MyCounterMacroServlet()), "/mycountermacro");
        context.addServlet(new ServletHolder(new WebHookServlet()), "/webhook/*");
        context.addServlet(new ServletHolder(new RegisterServlet(appKey, "refapp")), "/register");
        context.addServlet(new ServletHolder(new RegisterServlet(appKey, "confluence")), "/confluence-register");
        context.addServlet(new ServletHolder(new RegisterServlet(appKey, "jira")), "/jira-register");
        context.addServlet(new ServletHolder(new RegisterServlet(appKey, "refapp")), "/refapp-register");

        HandlerList list = new HandlerList();
        list.setHandlers(new Handler[] {staticResourceHandler, context});
        server.setHandler(list);
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
        if (args.length != 4)
        {
            System.err.println("Usage: java -jar remoteapps-sample-VERSION-standalone.jar APP_KEY, HOST_BASE_URL APP_BASE_URL INTERNAL_PORT");
            System.exit(1);
        }
        HttpServer server = new HttpServer(args[0], args[1], args[2], Integer.parseInt(args[3]));
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
