package com.atlassian.labs.remoteapps.plugin.product;

import com.atlassian.labs.remoteapps.spi.permission.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.spi.permission.scope.ApiScope;
import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 * Cross-product API Scope for retrieving the host application's configured Application Links.
 */
public class ReadAppLinksScope implements ApiScope
{
    private final RestApiScopeHelper scopeHelper;

    public ReadAppLinksScope()
    {
        scopeHelper = new RestApiScopeHelper(asList(
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/applicationlink", asList("get")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/applicationlinkInfo", asList("get")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entities", asList("get")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/entitylink", asList("get")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/listApplicationLinks", asList("get")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/manifest", asList("get")),
                new RestApiScopeHelper.RestScope("applinks", asList("1.0", "latest"), "/type/entity", asList("get"))
        ));
    }

    @Override
    public String getKey()
    {
        return "read_app_links";
    }

    @Override
    public String getName()
    {
        return "Read Application Links";
    }

    @Override
    public String getDescription()
    {
        return "Allows a Remote App to retrieve the host application's configured application links and entity links.";
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
