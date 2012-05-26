package com.atlassian.labs.remoteapps.installer;

import com.atlassian.event.api.EventListener;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;

import java.util.concurrent.CountDownLatch;

/**
* Listens for the remote app to start
*/
public class StartedListener
{
    private final String pluginKey;
    private final CountDownLatch latch;

    private volatile Exception cause;

    public StartedListener(String pluginKey, CountDownLatch latch)
    {
        this.pluginKey = pluginKey;
        this.latch = latch;
    }

    @EventListener
    public void onAppStart(RemoteAppStartedEvent event)
    {
        if (event.getRemoteAppKey().equals(pluginKey))
        {
            latch.countDown();
        }
    }

    @EventListener
    public void onAppStartFailed(RemoteAppStartFailedEvent event)
    {
        if (event.getRemoteAppKey().equals(pluginKey))
        {
            cause = event.getCause();
            latch.countDown();
        }
    }
    public Exception getFailedCause()
    {
        return cause;
    }
}
