package com.atlassian.plugin.connect.plugin.usermanagement;

public class ConnectAddOnUserInitException extends Exception
{
    public ConnectAddOnUserInitException(Exception cause)
    {
        super(cause);
    }

    public ConnectAddOnUserInitException(String message)
    {
        super(message);
    }
}
