package com.atlassian.plugin.remotable.plugin.oldscopes.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that allows Remotable Plugins to access the build information of the Confluence server.
 */
public final class ReadServerInformationScope extends ConfluenceScope
{
    protected ReadServerInformationScope()
    {
        super(ConfluencePermissions.READ_SERVER_INFORMATION,
                asList(
                        "getServerInfo"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/buildInfo", asList("get"))
                )
        );
    }
}
