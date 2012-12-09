package com.atlassian.plugin.remotable.container;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public final class Main
{
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final HttpServer server;
    private final Container container;

    public Main(ContainerCommandLine commandLine) throws Exception
    {
        server = new HttpServer(commandLine.getPort());
        container = new Container(new CommandLineContainerConfiguration(commandLine.getApplications()), server);
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

    public static void main(String[] args) throws Exception
    {
        final Main main = newMain(args);
        if (main == null)
        {
            return;
        }
        main.join();
    }

    public static Main newMain(String[] args) throws Exception
    {
        final ContainerCommandLine commandLine = ContainerCommandLine.parse(args);

        // configures the logging, do it as early as possible
        commandLine.getVerbosity().configure();

        checkJavaVersion();

        if (commandLine.isHelp())
        {
            ContainerCommandLine.printHelp();
            return null;
        }

        return new Main(commandLine);
    }

    private static void checkJavaVersion()
    {
        if (!SystemUtils.isJavaVersionAtLeast(170))
        {
            throw new IllegalStateException("The container requires Java 1.7 or greater. You're currently running Java " + SystemUtils.JAVA_VERSION_TRIMMED);
        }
        else
        {
            log.debug("Java version " + SystemUtils.JAVA_VERSION_TRIMMED + " is valid, continuing...");
        }
    }
}
