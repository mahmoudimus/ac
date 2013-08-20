package com.atlassian.plugin.connect.plugin.product;

import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import com.google.common.collect.ImmutableSet;

import static java.util.Arrays.asList;

/**
 * Cross-product API Scope for retrieving the host application's configured Application Links.
 */
public final class ReadAppLinksScope extends AbstractApiScope
{
    public ReadAppLinksScope()
    {
        super(Permissions.READ_APP_LINKS,
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/applicationlink", asList("get")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/applicationlinkInfo", asList("get")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entities", asList("get")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink", asList("get")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/listApplicationLinks", asList("get")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/manifest", asList("get")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/type/entity", asList("get"))
                )));
    }
}
