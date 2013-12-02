package com.atlassian.plugin.connect.test.scope;

import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class UnrequestedTestApiScope extends AbstractApiScope
{
    public UnrequestedTestApiScope()
    {
        super("unrequested_scope",
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("remoteplugintest", asList("latest", "1"), "/unauthorisedscope", asList("GET"))
                )));
    }
}
