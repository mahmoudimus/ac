package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.permission.AbstractPermission;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableSet;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.remotable.api.InstallationMode.LOCAL;
import static com.atlassian.plugin.remotable.api.InstallationMode.REMOTE;
import static java.util.Arrays.asList;

public final class ModifyAppLinkScope extends AbstractPermission implements ApiScope
{
    private final RestApiScopeHelper scopeHelper;

    public ModifyAppLinkScope()
    {
        super(Permissions.MODIFY_APP_LINKS, ImmutableSet.of(LOCAL, REMOTE));
        scopeHelper = new RestApiScopeHelper(asList(
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink/primary", asList("POST")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink", asList("PUT", "DELETE"))
        ));
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return scopeHelper.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return scopeHelper.getApiResourceInfos();
    }
}