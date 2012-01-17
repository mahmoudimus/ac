package com.atlassian.labs.remoteapps.sample;


import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.apputils.RegisterServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import static com.atlassian.labs.remoteapps.apputils.Environment.getEnv;
import static com.atlassian.labs.remoteapps.apputils.Environment.getEnvAsInt;

/**
 *
 */
public class HttpServer
{

    private final Server server;

    public HttpServer()
    {
        server = new Server(getEnvAsInt("PORT"));

        ResourceHandler staticResourceHandler = new ResourceHandler();
        String resourceBase = getClass().getResource("/static/").toString();
        System.out.println("resource base: " + resourceBase);
        staticResourceHandler.setResourceBase(resourceBase);
        staticResourceHandler.setDirectoriesListed(true);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        context.setResourceBase(resourceBase);
        String appKey = getEnv("OAUTH_LOCAL_KEY");
        OAuthContext oauthContext = new OAuthContext();

        context.addServlet(new ServletHolder(new InfoServlet(appKey)), "/");
        context.addServlet(new ServletHolder(new MyAdminServlet(oauthContext)), "/myadmin");
        context.addServlet(new ServletHolder(new MyMacroServlet()), "/mymacro");
        context.addServlet(new ServletHolder(new MyImageMacroServlet(oauthContext)), "/myimagemacro");
        context.addServlet(new ServletHolder(new MySlowMacroServlet()), "/myslowmacro");
        context.addServlet(new ServletHolder(new MyCounterMacroServlet()), "/mycountermacro");
        context.addServlet(new ServletHolder(new WebHookServlet()), "/webhook/*");
        context.addServlet(new ServletHolder(new MacroResetServlet(appKey, oauthContext)), "/macro-reset");
        context.addServlet(new ServletHolder(new RegisterServlet("sample-descriptor-refapp.mu.xml", oauthContext)), "/register");
        context.addServlet(new ServletHolder(new RegisterServlet("sample-descriptor-confluence.mu.xml", oauthContext)), "/confluence-register");
        context.addServlet(new ServletHolder(new RegisterServlet("sample-descriptor-jira.mu.xml", oauthContext)), "/jira-register");
        context.addServlet(new ServletHolder(new RegisterServlet("sample-descriptor-refapp.mu.xml", oauthContext)), "/refapp-register");

        HandlerList list = new HandlerList();
        list.setHandlers(new Handler[] {staticResourceHandler, context});
        server.setHandler(list);
        start();
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
        HttpServer server = new HttpServer();
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
