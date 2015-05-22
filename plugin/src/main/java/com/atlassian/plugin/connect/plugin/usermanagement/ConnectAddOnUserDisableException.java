package com.atlassian.plugin.connect.plugin.usermanagement;

public class ConnectAddOnUserDisableException extends Exception
{
    public ConnectAddOnUserDisableException(Exception cause)
    {
        super(cause);
    }

    public ConnectAddOnUserDisableException(String message) {
        super(message);
    }
}
