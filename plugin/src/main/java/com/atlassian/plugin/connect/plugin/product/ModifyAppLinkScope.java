package com.atlassian.plugin.connect.plugin.product;

import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import com.google.common.collect.ImmutableSet;


import static java.util.Arrays.asList;

public final class ModifyAppLinkScope extends AbstractApiScope
{
    public ModifyAppLinkScope()
    {
        super(Permissions.MODIFY_APP_LINKS,
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink/primary", asList("POST")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink", asList("PUT", "DELETE"))
                )));
    }
}