package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.ServerInfo;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

import java.io.InputStream;

/**
 */
public interface ConfluenceAdminClient
{
    @RequirePermission(ConfluencePermissions.READ_SERVER_INFORMATION)
    Promise<ServerInfo> getServerInfo();

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<InputStream> exportSite(boolean exportAttachments);

    @RequirePermission(ConfluencePermissions.MANAGE_INDEX)
    Promise<Boolean> flushIndexQueue();

    @RequirePermission(ConfluencePermissions.MANAGE_INDEX)
    Promise<Void> clearIndexQueue();
}
