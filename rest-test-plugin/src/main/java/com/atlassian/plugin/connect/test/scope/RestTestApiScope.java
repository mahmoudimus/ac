package com.atlassian.plugin.connect.test.scope;

import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import com.google.common.collect.ImmutableSet;

import static java.util.Arrays.asList;

public final class RestTestApiScope extends AbstractApiScope
{
    public RestTestApiScope()
    {
        super("resttest",
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/user", asList("GET"))
                )));
    }
}
