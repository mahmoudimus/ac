package com.atlassian.plugin.connect.api.lifecycle;

public class ConnectAddonDisableException extends Exception
{
    public ConnectAddonDisableException(Exception cause)
    {
        super(cause);
    }
}
