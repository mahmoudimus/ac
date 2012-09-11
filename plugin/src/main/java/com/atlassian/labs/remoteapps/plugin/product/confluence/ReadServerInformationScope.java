package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;
import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that allows Remote Apps to access the build information of the Confluence server.
 */
public class ReadServerInformationScope extends ConfluenceScope
{
    protected ReadServerInformationScope()
    {
        super(ConfluencePermission.READ_SERVER_INFORMATION,
        asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/buildInfo", asList("get"))
            )
        );
    }
}
