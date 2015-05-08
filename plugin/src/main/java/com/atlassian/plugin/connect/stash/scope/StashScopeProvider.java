package com.atlassian.plugin.connect.stash.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.ProductScopeProvider;
import com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@StashComponent
public class StashScopeProvider implements ProductScopeProvider
{
    @Override
    public Map<ScopeName, AddOnScope> getScopes()
    {
        ImmutableList<String> products = ImmutableList.of("stash");

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
        return StashScopeProvider.class.getResource("/com/atlassian/connect/stash/scopes." + product + ".json");
    }
}
