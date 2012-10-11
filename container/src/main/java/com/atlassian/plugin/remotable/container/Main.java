package com.atlassian.plugin.remotable.container;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
public class Main
{
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final HttpServer server;
    private final Container container;

    public Main(String[] apps)
        throws Exception
    {
        server = new HttpServer();
        container = new Container(server, apps);
        container.start();
        List<String> lines = newArrayList();
        lines.add("Remote plugin container started on port " + server.getAppPort());
        lines.add("");
        lines.add("Available plugins:");
        for (String appKey : server.getContextNames())
        {
            lines.add("\t" + server.getLocalMountBaseUrl(appKey));
        }
        log.info(StringUtils.join(lines, "\n"));
    }

    public void join()
    {
        try
        {
            server.join();
        }
        catch (InterruptedException e)
        {
            container.stop();
        }
    }

    public void stop()
    {
        server.stop();
        container.stop();
    }

    public static void main(String[] apps) throws Exception
    {
        new Main(apps).join();
    }
}
