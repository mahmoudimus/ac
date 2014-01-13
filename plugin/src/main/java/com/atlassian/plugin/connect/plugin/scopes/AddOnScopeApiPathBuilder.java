package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.plugin.scopes.beans.AddOnScopeBean;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import java.util.ArrayList;
import java.util.Collection;

public class AddOnScopeApiPathBuilder
{
    Collection<RestApiScopeHelper.RestScope> restResources = new ArrayList<RestApiScopeHelper.RestScope>();

    public AddOnScopeApiPathBuilder withRestPaths(AddOnScopeBean.RestPathBean restPathBean, Collection<String> methods)
    {
        for (String basePath : restPathBean.getBasePaths())
        {
            restResources.add(new RestApiScopeHelper.RestScope(restPathBean.getName(), restPathBean.getVersions(), basePath, methods, true));
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
