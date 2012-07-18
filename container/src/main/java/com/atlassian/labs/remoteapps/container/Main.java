package com.atlassian.labs.remoteapps.container;

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
    public static void main(String[] apps) throws Exception
    {
        HttpServer server = new HttpServer();
        server.start();
        Container container = new Container(server, apps);
        container.start();
        try
        {
            List<String> lines = newArrayList();
            lines.add("Remote App container started on port " + server.getAppPort());
            lines.add("");
            lines.add("Available remote apps:");
            for (String appKey : server.getContextNames())
            {
                lines.add("\t" + server.getLocalMountBaseUrl(appKey));
            }
            log.info(StringUtils.join(lines, "\n"));
            server.join();
        }
        catch (InterruptedException e)
        {
            container.stop();
            // ignore
        }
    }

}
