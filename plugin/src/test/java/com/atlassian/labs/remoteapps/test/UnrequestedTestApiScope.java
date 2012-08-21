package com.atlassian.labs.remoteapps.test;

import com.atlassian.labs.remoteapps.spi.permission.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.spi.permission.scope.ApiScope;
import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

public class UnrequestedTestApiScope implements ApiScope
{
    private final RestApiScopeHelper validator = new RestApiScopeHelper(asList(
            new RestApiScopeHelper.RestScope("remoteapptest", asList("latest", "1"), "/unauthorisedscope", asList("GET"))
    ));

    @Override
    public String getKey()
    {
        return "unrequested_scope";
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return validator.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return validator.getApiResourceInfos();
    }

    @Override
    public String getName()
    {
        return "Unrequested API Scope";
    }

    @Override
    public String getDescription()
    {
        return "An API Scope that never gets used";
    }
}
