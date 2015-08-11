package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.event.directory.RemoteDirectorySynchronisedEvent;
import com.atlassian.event.api.EventListener;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Calls in to the ConnectCrowdSyncService after a directory sync has finished,
 * to finalise add-on user setup.
 *
 * We can't use one of our components directly, because the event system
 * appears to create a new instance instead of using the existing component.
 */
public class ConnectCrowdSyncListener
{
    private final ConnectCrowdSyncService syncService;

    @Autowired
    public ConnectCrowdSyncListener(ConnectCrowdSyncService syncService)
    {
        this.syncService = syncService;
    }

    @EventListener
    public void reportSyncedUser(RemoteDirectorySynchronisedEvent event)
    {
        syncService.handleSync();
    }

}
