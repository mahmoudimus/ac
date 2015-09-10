package com.atlassian.plugin.connect.spi.module.provider;

public class ConnectModuleValidationException extends Exception
{
    public ConnectModuleValidationException(String moduleType, String error)
    {
        super("Could not validate modules of type " + moduleType + ". " + error);
    }
}
