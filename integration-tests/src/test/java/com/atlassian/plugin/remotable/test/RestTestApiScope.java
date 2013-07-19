package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.plugin.remotable.api.InstallationMode.LOCAL;
import static com.atlassian.plugin.remotable.api.InstallationMode.REMOTE;
import static java.util.Arrays.asList;

public final class RestTestApiScope extends AbstractApiScope
{
    public RestTestApiScope()
    {
        super("resttest",
                ImmutableSet.of(LOCAL, REMOTE),
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/user", asList("GET"))
                )));
    }
}
