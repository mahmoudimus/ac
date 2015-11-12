package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugin.connect.spi.web.ConnectWebFragmentLocationBlacklistModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WebFragmentLocationBlacklistImpl implements WebFragmentLocationBlacklist
{
    private final PluginAccessor pluginAccessor;

    @Autowired
    public WebFragmentLocationBlacklistImpl(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Set<String> blacklistedWebPanelLocations()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectWebFragmentLocationBlacklistModuleDescriptor.class)
                .stream()
                .map(ConnectWebFragmentLocationBlacklistModuleDescriptor::getModule)
                .map(ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist::getWebPanelBlacklistedLocations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> blacklistedWebItemLocations()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectWebFragmentLocationBlacklistModuleDescriptor.class)
                .stream()
                .map(ConnectWebFragmentLocationBlacklistModuleDescriptor::getModule)
                .map(ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist::getWebItemBlacklistedLocations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
