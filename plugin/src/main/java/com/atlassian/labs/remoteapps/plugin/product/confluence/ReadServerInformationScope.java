package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that allows Remote Apps to access the build information of the Confluence server.
 */
public class ReadServerInformationScope extends ConfluenceScope
{
    protected ReadServerInformationScope()
    {
        super(asList(
                "getServerInfo"
        ),
        asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/buildInfo", asList("get"))
            )
        );
    }

    @Override
    public String getKey()
    {
        return "read_server_information";
    }

    @Override
    public String getName()
    {
        return "Read Server Information";
    }

    @Override
    public String getDescription()
    {
        return "View Confluence version number, base URL and build information";
    }
}
