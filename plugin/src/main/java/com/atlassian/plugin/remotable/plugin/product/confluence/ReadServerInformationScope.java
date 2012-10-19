package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that allows Remotable Plugins to access the build information of the Confluence server.
 */
public class ReadServerInformationScope extends ConfluenceScope
{
    protected ReadServerInformationScope()
    {
        super(ConfluencePermissions.READ_SERVER_INFORMATION,
        asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/buildInfo", asList("get"))
            )
        );
    }
}
