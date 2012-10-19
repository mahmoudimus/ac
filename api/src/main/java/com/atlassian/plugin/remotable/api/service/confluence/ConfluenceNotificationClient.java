package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentType;
import com.atlassian.plugin.remotable.api.service.confluence.domain.User;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 */
public interface ConfluenceNotificationClient
{
    @RequirePermission(ConfluencePermissions.MANAGE_WATCHERS)
    Promise<Boolean> watchPage(long pageId);

    @RequirePermission(ConfluencePermissions.MANAGE_WATCHERS)
    Promise<Boolean> watchPageForUser(long pageId, String userName);

    @RequirePermission(ConfluencePermissions.MANAGE_WATCHERS)
    Promise<Boolean> watchSpace(String spaceKey);

    @RequirePermission(ConfluencePermissions.MANAGE_WATCHERS)
    Promise<Boolean> removePageWatch(long pageId);

    @RequirePermission(ConfluencePermissions.MANAGE_WATCHERS)
    Promise<Boolean> removeSpaceWatch(String spaceKey);

    @RequirePermission(ConfluencePermissions.MANAGE_WATCHERS)
    Promise<Boolean> removePageWatchForUser(long pageId, String userName);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Boolean> isWatchingPage(long pageId, String userName);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<User>> getWatchersForPage(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Boolean> isWatchingSpace(String spaceKey, String userName);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Boolean> isWatchingSpaceForType(String spaceKey, ContentType contentType, String userName);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<User>> getWatchersForSpace(String spaceKey);
}
