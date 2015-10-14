package com.atlassian.plugin.connect.spi.module;

/**
 * An exception thrown when syntactic or semantic validation of a descriptor module fails.
 */
public class ConnectModuleValidationException extends Exception
{
    public ConnectModuleValidationException(String moduleType, String error)
    {
        super("Could not validate modules of type " + moduleType + ". " + error);
    }
}
