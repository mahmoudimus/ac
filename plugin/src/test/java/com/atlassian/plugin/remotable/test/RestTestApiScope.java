package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 *
 */
public class RestTestApiScope implements ApiScope
{
    private final RestApiScopeHelper validator = new RestApiScopeHelper(asList(
        new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/user", asList("GET"))
    ));

    @Override
    public String getKey()
    {
        return "resttest";
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
        return "Rest Test";
    }

    @Override
    public String getDescription()
    {
        return "A test resource";
    }
}
