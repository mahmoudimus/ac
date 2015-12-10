package com.atlassian.plugin.connect.api.lifecycle;

/**
 * Exception that is thrown when disablement of a connect add-on fails,
 * e.g. when the associated user can not be disabled.
 */
public class ConnectAddonDisableException extends Exception
{
    public ConnectAddonDisableException(Exception cause)
    {
        super(cause);
    }
}
