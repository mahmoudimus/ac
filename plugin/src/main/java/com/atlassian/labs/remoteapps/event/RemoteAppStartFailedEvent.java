package com.atlassian.labs.remoteapps.event;

/**
 *
 */
public class RemoteAppStartFailedEvent extends RemoteAppEvent
{
    private final Exception cause;

    public RemoteAppStartFailedEvent(String appKey, Exception ex)
    {
        super(appKey);
        cause = ex;
    }

    public Exception getCause()
    {
        return cause;
    }
}
