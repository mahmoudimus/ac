package com.atlassian.plugin.connect.plugin.usermanagement;

public class ConnectAddonUserUpdateException extends Exception
{
    public ConnectAddonUserUpdateException(Exception cause)
    {
        super(cause);
    }

    public ConnectAddonUserUpdateException(String message) {
        super(message);
    }
}
