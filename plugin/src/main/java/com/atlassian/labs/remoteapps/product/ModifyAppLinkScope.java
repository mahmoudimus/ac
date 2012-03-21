package com.atlassian.labs.remoteapps.product;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

public class ModifyAppLinkScope implements ApiScope
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
    public String getName()
    {
        return "Modify Owned App Link";
    }

    @Override
    public String getDescription()
    {
        return "Allows a Remote App to modify the details of its own configured Application Link.";
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
