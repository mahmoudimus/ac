package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.domain.ServerInfo;
import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;

import java.io.InputStream;

/**
 */
public interface ConfluenceAdminClient
{
    @RequirePermission(ConfluencePermission.READ_SERVER_INFORMATION)
    Promise<ServerInfo> getServerInfo();

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<InputStream> exportSite(boolean exportAttachments);

    @RequirePermission(ConfluencePermission.MANAGE_INDEX)
    Promise<Boolean> flushIndexQueue();

    @RequirePermission(ConfluencePermission.MANAGE_INDEX)
    Promise<Void> clearIndexQueue();
}
