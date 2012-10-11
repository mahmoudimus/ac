package com.atlassian.plugin.remotable.junit;

import java.util.Locale;

public enum Mode
{
    CONTAINER,
    INSTALL,
    PROPERTY;

    static Mode get(UniversalBinaries annotation)
    {
        if (annotation.mode().equals(PROPERTY))
        {
            return valueOf(System.getProperty("pluginMode", Mode.INSTALL.name()).toUpperCase(Locale.ENGLISH));
        }
        else
        {
            return annotation.mode();
        }
    }
}
