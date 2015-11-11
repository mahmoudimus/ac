package com.atlassian.plugin.connect.plugin.auth.scope.whitelist;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.ScopeService;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PluggableScopeService implements ScopeService
{

    private final PluginAccessor pluginAccessor;

    @Autowired
    public PluggableScopeService(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Collection<AddOnScope> build()
    {
        Collection<ConnectApiScopeWhitelist> whitelists = pluginAccessor.getModules(
                new ModuleDescriptorOfClassPredicate<>(ConnectApiScopeWhitelistModuleDescriptor.class));

        Map<ScopeName, AddOnScope> scopes = new HashMap<>();
        for (ConnectApiScopeWhitelist whitelist : whitelists)
        {
            AddOnScopeLoadJsonFileHelper.combineScopes(scopes, whitelist.getScopes());
        }

        List<AddOnScope> scopeList = new ArrayList<>(scopes.values());
        Collections.sort(scopeList);

        return scopeList;
    }
}
