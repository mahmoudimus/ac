package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.PluginParseException;

public class InvalidAddonConfigurationException extends PluginParseException
{
    public InvalidAddonConfigurationException(String message)
    {
        super(message);
    }

    public InvalidAddonConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
