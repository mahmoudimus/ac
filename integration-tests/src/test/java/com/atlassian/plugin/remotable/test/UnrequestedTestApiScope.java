package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableSet;

import static java.util.Arrays.asList;

public final class UnrequestedTestApiScope extends AbstractApiScope
{
    public UnrequestedTestApiScope()
    {
        super("unrequested_scope",
                ImmutableSet.of(InstallationMode.LOCAL),
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/unauthorisedscope", asList("GET"))
                )));
    }
}
