package com.atlassian.plugin.connect.spi.lifecycle;

public class ConnectAddonDisableException extends Exception
{
    public ConnectAddonDisableException(Exception cause)
    {
        super(cause);
    }
}
