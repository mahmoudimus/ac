package com.atlassian.labs.remoteapps.junit;

public enum Mode
{
    CONTAINER,
    INSTALL,
    PROPERTY;

    static Mode get(UniversalBinaries annotation)
    {
        if (annotation.mode().equals(PROPERTY))
        {
            return valueOf(System.getProperty("pluginMode", Mode.INSTALL.name()));
        }
        else
        {
            return annotation.mode();
        }
    }
}
