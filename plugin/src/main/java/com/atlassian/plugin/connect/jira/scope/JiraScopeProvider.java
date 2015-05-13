package com.atlassian.plugin.connect.jira.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.ProductScopeProvider;
import com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@JiraComponent
public class JiraScopeProvider implements ProductScopeProvider
{
    @Override
    public Map<ScopeName, AddOnScope> getScopes()
    {
        // TODO: Tempo is only a temporary addition to facilitate the migration to Connect
        ImmutableList<String> products = ImmutableList.of("jira", "jiraagile", "tempo");

        Map<ScopeName, AddOnScope> keyToScope = new HashMap<>();
        try
        {
            for (String product : products)
            {
                AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(keyToScope, resourceURL(product));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return keyToScope;
    }

    private static URL resourceURL(String product)
    {
        return JiraScopeProvider.class.getResource("/com/atlassian/connect/jira/scopes." + product + ".json");
    }
}
