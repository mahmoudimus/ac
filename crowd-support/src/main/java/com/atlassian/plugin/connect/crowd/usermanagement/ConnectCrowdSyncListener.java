package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.event.api.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ConnectCrowdSyncListener
{
    private static final Logger log = LoggerFactory.getLogger(ConnectCrowdSyncListener.class);
    private final ConnectCrowdSyncService syncService;

    @Autowired
    public ConnectCrowdSyncListener(ConnectCrowdSyncService syncService)
    {
        this.syncService = syncService;
    }

    @EventListener
    public void reportSyncedUser(UserUpdatedEvent event)
    {
        syncService.handleSync(event.getUser().getName());
    }

}
