package com.atlassian.plugin.connect.confluence.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.ProductScopeProvider;
import com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@ConfluenceComponent
public class ConfluenceScopeProvider implements ProductScopeProvider
{
    @Override
    public Map<ScopeName, AddOnScope> getScopes()
    {
        ImmutableList<String> products = ImmutableList.of("confluence");

        Map<ScopeName, AddOnScope> keyToScope = new HashMap<>();
        try
        {
            for (String product : products)
            {
                AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(keyToScope, resourceLocation(product));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return keyToScope;
    }

    private static URL resourceLocation(String product)
    {
        return ConfluenceScopeProvider.class.getResource("/com/atlassian/connect/confluence/scopes." + product + ".json");
    }
}
