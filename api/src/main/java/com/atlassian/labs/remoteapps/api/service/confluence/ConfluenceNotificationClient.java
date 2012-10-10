package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.domain.ContentType;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.User;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 */
public interface ConfluenceNotificationClient
{
    @RequirePermission(ConfluencePermission.MANAGE_WATCHERS)
    Promise<Boolean> watchPage(long pageId);

    @RequirePermission(ConfluencePermission.MANAGE_WATCHERS)
    Promise<Boolean> watchPageForUser(long pageId, String userName);

    @RequirePermission(ConfluencePermission.MANAGE_WATCHERS)
    Promise<Boolean> watchSpace(String spaceKey);

    @RequirePermission(ConfluencePermission.MANAGE_WATCHERS)
    Promise<Boolean> removePageWatch(long pageId);

    @RequirePermission(ConfluencePermission.MANAGE_WATCHERS)
    Promise<Boolean> removeSpaceWatch(String spaceKey);

    @RequirePermission(ConfluencePermission.MANAGE_WATCHERS)
    Promise<Boolean> removePageWatchForUser(long pageId, String userName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Boolean> isWatchingPage(long pageId, String userName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<User>> getWatchersForPage(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Boolean> isWatchingSpace(String spaceKey, String userName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Boolean> isWatchingSpaceForType(String spaceKey, ContentType contentType, String userName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<User>> getWatchersForSpace(String spaceKey);
}
