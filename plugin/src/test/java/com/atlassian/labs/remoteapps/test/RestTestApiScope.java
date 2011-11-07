package com.atlassian.labs.remoteapps.test;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RestApiScope;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 *
 */
public class RestTestApiScope implements ApiScope
{
    private final RestApiScope validator = new RestApiScope(asList(
        new RestApiScope.RestScope("remoteapptest", asList("latest", "1"), "/", asList("GET"))
    ));

    @Override
    public boolean allow(HttpServletRequest request)
    {
        return validator.allow(request);
    }
}
