package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.plugin.remotable.api.InstallationMode.LOCAL;
import static com.atlassian.plugin.remotable.api.InstallationMode.REMOTE;
import static java.util.Arrays.asList;

public final class ModifyAppLinkScope extends AbstractApiScope
{
    public ModifyAppLinkScope()
    {
        super(Permissions.MODIFY_APP_LINKS,
                ImmutableSet.of(LOCAL, REMOTE),
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink/primary", asList("POST")),
                        new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink", asList("PUT", "DELETE"))
                )));
    }
}