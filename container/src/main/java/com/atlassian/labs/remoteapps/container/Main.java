package com.atlassian.labs.remoteapps.container;

import org.apache.log4j.BasicConfigurator;

/**
 *
 */
public class Main
{
    public static void main(String[] apps) throws Exception
    {
        HttpServer server = new HttpServer();
        server.start();
        Container container = new Container(server, apps);
        container.start();
        try
        {
            System.out.println("Remote App container started on port " + server.getAppPort());
            System.out.println("\nAvailable remote apps:");
            for (String appKey : server.getContextNames())
            {
                System.out.println("\t" + server.getLocalMountBaseUrl(appKey));
            }
            server.join();
        }
        catch (InterruptedException e)
        {
            container.stop();
            // ignore
        }
    }

}
