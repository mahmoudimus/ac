package com.atlassian.labs.remoteapps.plugin.product;

import com.atlassian.labs.remoteapps.spi.permission.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.spi.permission.scope.ApiScope;
import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

public class ModifyAppLinkScope extends AbstractMutableApiScope
{
    private final RestApiScopeHelper scopeHelper;

    public ModifyAppLinkScope()
    {
        scopeHelper = new RestApiScopeHelper(asList(
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink/primary", asList("POST")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink", asList("PUT", "DELETE"))
        ));
    }

    @Override
    public String getKey()
    {
        return "modify_app_link";
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
