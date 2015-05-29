package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper;
import com.atlassian.plugin.connect.spi.scope.ProductScopeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ScopeServiceImpl implements ScopeService
{
    private final ProductScopeProvider productScopeProvider;

    @Autowired
    public ScopeServiceImpl(ProductScopeProvider productScopeProvider)
    {
        this.productScopeProvider = checkNotNull(productScopeProvider);
    }

    @Override
    public Collection<AddOnScope> build() throws IOException
    {
        Map<ScopeName, AddOnScope> scopes = new HashMap<>();

        AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(scopes, resourceLocation("common"));
        // TODO ACDEV-1214: don't load integration_test scopes in prod
        AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(scopes, resourceLocation("integration-test"));

        AddOnScopeLoadJsonFileHelper.combineProductScopes(scopes, productScopeProvider.getScopes());

        List<AddOnScope> scopeList = new ArrayList<>(scopes.values());
        Collections.sort(scopeList);

        return scopeList;
    }

    private static URL resourceLocation(String product)
    {
        return ScopeServiceImpl.class.getResource("/com/atlassian/connect/scopes." + product + ".json");
    }
}
