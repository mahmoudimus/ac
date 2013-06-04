package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.spi.permission.AbstractPermission;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

public final class UnrequestedTestApiScope extends AbstractPermission implements ApiScope
{
    private final RestApiScopeHelper validator = new RestApiScopeHelper(asList(
            new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/unauthorisedscope", asList("GET"))
    ));

    public UnrequestedTestApiScope()
    {
        super("unrequested_scope");
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
}
