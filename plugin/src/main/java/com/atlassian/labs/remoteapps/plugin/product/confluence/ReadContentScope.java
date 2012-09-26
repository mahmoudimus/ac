package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;
import com.atlassian.labs.remoteapps.spi.permission.scope.DownloadScopeHelper;
import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadContentScope extends ConfluenceScope
{
    public ReadContentScope()
    {
        super(ConfluencePermission.READ_CONTENT,
            asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space", asList("get"))
            ),
            new DownloadScopeHelper("/download/temp/")
        );
    }
}
