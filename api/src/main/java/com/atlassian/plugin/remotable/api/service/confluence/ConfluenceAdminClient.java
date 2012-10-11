package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.ServerInfo;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

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
