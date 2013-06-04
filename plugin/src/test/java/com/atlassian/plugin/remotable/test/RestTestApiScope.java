package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.spi.permission.AbstractPermission;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableSet;

import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.remotable.api.InstallationMode.LOCAL;
import static com.atlassian.plugin.remotable.api.InstallationMode.REMOTE;
import static java.util.Arrays.asList;

public final class RestTestApiScope extends AbstractPermission implements ApiScope
{
    private final RestApiScopeHelper validator = new RestApiScopeHelper(asList(
            new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/user", asList("GET"))
    ));

    public RestTestApiScope()
    {
        super("resttest", ImmutableSet.of(LOCAL, REMOTE));
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
