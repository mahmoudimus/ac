package com.atlassian.plugin.connect.core.applinks;

public class NotConnectAddonException extends RuntimeException
{
    public NotConnectAddonException(String message)
    {
        super(message);
    }

    public NotConnectAddonException(String message, Throwable throwable)
    {
        super(message, throwable);
    }

    public NotConnectAddonException(Throwable throwable)
    {
        super(throwable);
    }
}
