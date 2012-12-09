package com.atlassian.plugin.remotable.container;

import org.apache.log4j.PropertyConfigurator;

import java.net.URL;

public enum Verbosity
{
    NONE("/log4j.properties"),
    V("/log4j-v.properties"),
    VV("/log4j-vv.properties"),
    VVV("/log4j-vvv.properties");

    private final String name;

    private Verbosity(String name)
    {
        this.name = name;
    }

    public final void configure()
    {
        configure(name);
    }

    private void configure(String name)
    {
        PropertyConfigurator.configure(getResource(name));
    }

    private URL getResource(String name)
    {
        return this.getClass().getResource(name);
    }
}
