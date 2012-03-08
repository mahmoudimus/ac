package com.atlassian.labs.remoteapps.product.confluence;

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
        ));
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
