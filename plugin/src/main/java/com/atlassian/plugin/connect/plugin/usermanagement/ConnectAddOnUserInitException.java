package com.atlassian.plugin.connect.plugin.usermanagement;

/**
 * todo: write javadoc
 */
public class ConnectAddOnUserInitException extends RuntimeException
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
