package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.DownloadScopeHelper;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadContentScope extends ConfluenceScope
{
    public ReadContentScope()
    {
        super(ConfluencePermissions.READ_CONTENT,
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
