package com.atlassian.plugin.remotable.container;

import com.atlassian.fugue.Option;
import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ContainerCommandLine
{
    static final String HELP = "help";
    static final String VERBOSITY_3 = "vvv";
    static final String VERBOSITY_2 = "vv";
    static final String VERBOSITY_1 = "v";
    static final String PORT = "p";

    private final boolean help;
    private final Verbosity verbosity;
    private final Option<Integer> port;
    private final Iterable<String> applications;

    ContainerCommandLine(boolean help, Verbosity verbosity, Option<Integer> port, Iterable<String> applications)
    {
        this.help = help;
        this.verbosity = checkNotNull(verbosity);
        this.port = checkNotNull(port);
        this.applications = checkNotNull(applications);
    }

    public boolean isHelp()
    {
        return help;
    }

    public Verbosity getVerbosity()
    {
        return verbosity;
    }

    Option<Integer> getPort()
    {
        return port;
    }

    public Iterable<String> getApplications()
    {
        return applications;
    }

    static void printHelp()
    {
        new HelpFormatter().printHelp("java -jar container.jar [options] [apps...]", getOptions(), false);
    }

    static ContainerCommandLine parse(String args[])
    {
        final Options options = getOptions();
        try
        {
            final CommandLine cmd = new GnuParser().parse(options, args);
            return new ContainerCommandLine(
                    isHelp(cmd),
                    getVerbosity(cmd),
                    getPort(cmd),
                    getApplications(cmd));
        }
        catch (ParseException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static Options getOptions()
    {
        final Options options = new Options();
        options.addOption(getHelpOption());
        options.addOptionGroup(new OptionGroup()
                .addOption(getVerbosity1Option())
                .addOption(getVerbosity2Option()))
                .addOption(getVerbosity3Option());
        options.addOption(getPortOption());
        return options;
    }

    @SuppressWarnings("static-access")
    private static org.apache.commons.cli.Option getHelpOption()
    {
        return OptionBuilder
                .withLongOpt("help")
                .withDescription("Prints this help")
                .create();
    }

    @SuppressWarnings("static-access")
    private static org.apache.commons.cli.Option getVerbosity1Option()
    {
        return OptionBuilder
                .withDescription("Verbose")
                .create(VERBOSITY_1);
    }

    @SuppressWarnings("static-access")
    private static org.apache.commons.cli.Option getVerbosity2Option()
    {
        return OptionBuilder
                .withDescription("Very Verbose")
                .create(VERBOSITY_2);
    }

    @SuppressWarnings("static-access")
    private static org.apache.commons.cli.Option getVerbosity3Option()
    {
        return OptionBuilder
                .withDescription("Very Very Verbose")
                .create(VERBOSITY_3);
    }

    @SuppressWarnings("static-access")
    private static org.apache.commons.cli.Option getPortOption()
    {
        return OptionBuilder
                .withLongOpt("port")
                .withDescription("Defines the port the container will listen on.")
                .hasArg()
                .withArgName("PORT")
                .withType(Number.class)
                .create('p');
    }

    private static boolean isHelp(CommandLine cmd) throws ParseException
    {
        return cmd.hasOption(HELP);
    }

    private static Verbosity getVerbosity(CommandLine cmd) throws ParseException
    {
        if (cmd.hasOption(VERBOSITY_3))
        {
            return Verbosity.VVV;
        }
        else if(cmd.hasOption(VERBOSITY_2))
        {
            return Verbosity.VV;
        }
        else if(cmd.hasOption(VERBOSITY_1))
        {
            return Verbosity.V;
        }
        else
        {
            return Verbosity.NONE;
        }
    }

    private static Option<Integer> getPort(CommandLine cmd) throws ParseException
    {
        return option(cmd.hasOption(PORT) ? doGetPort(cmd) : null);
    }

    private static int doGetPort(CommandLine cmd) throws ParseException
    {
        return ((Long) cmd.getParsedOptionValue(PORT)).intValue();
    }

    private static Iterable<String> getApplications(CommandLine cmd) throws ParseException
    {
        return ImmutableList.copyOf(cmd.getArgs());
    }
}
