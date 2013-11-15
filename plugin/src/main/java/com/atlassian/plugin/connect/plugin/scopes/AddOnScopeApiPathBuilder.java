package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import java.util.ArrayList;
import java.util.Collection;

public class AddOnScopeApiPathBuilder
{
    Collection<RestApiScopeHelper.RestScope> restResources = new ArrayList<RestApiScopeHelper.RestScope>();

    public AddOnScopeApiPathBuilder withRestPaths(String name, Collection<String> basePaths, Collection<String> versions, Collection<String> methods)
    {
        for (String basePath : basePaths)
        {
            restResources.add(new RestApiScopeHelper.RestScope(name, versions, basePath, methods));
        }

        return this;
    }

    public Collection<AddOnScopeApiPath> build()
    {
        Collection<AddOnScopeApiPath> paths = new ArrayList<AddOnScopeApiPath>();

        if (!restResources.isEmpty())
        {
            paths.add(new AddOnScopeApiPath.RestApiPath(restResources));
        }

        return paths;
    }
}
