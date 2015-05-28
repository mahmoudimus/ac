package com.atlassian.plugin.connect.core.capabilities.descriptor;

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
