package com.atlassian.plugin.connect.plugin.web.blacklist;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named
@ExportAsDevService
public class WebFragmentLocationBlacklistImpl implements WebFragmentLocationBlacklist
{
    private final PluginAccessor pluginAccessor;

    @Autowired
    public WebFragmentLocationBlacklistImpl(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Set<String> getBlacklistedWebPanelLocations()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectWebFragmentLocationBlacklistModuleDescriptor.class)
                .stream()
                .map(new Function<ConnectWebFragmentLocationBlacklistModuleDescriptor, ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist>()
                {
                    @Override
                    public ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist apply(ConnectWebFragmentLocationBlacklistModuleDescriptor connectWebFragmentLocationBlacklistModuleDescriptor)
                    {
                        return connectWebFragmentLocationBlacklistModuleDescriptor.getModule();
                    }
                })
                .map(new Function<ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist, ImmutableSet<String>>() {
                    @Override
                    public ImmutableSet<String> apply(ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist connectWebFragmentLocationBlacklist)
                    {
                        return connectWebFragmentLocationBlacklist.getWebPanelBlacklistedLocations();
                    }
                })
                .flatMap(new Function<ImmutableSet<String>, Stream<String>>()
                {
                    @Override
                    public Stream<String> apply(ImmutableSet<String> strings)
                    {
                        return strings.stream();
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getBlacklistedWebItemLocations()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectWebFragmentLocationBlacklistModuleDescriptor.class)
                .stream()
                .map(new Function<ConnectWebFragmentLocationBlacklistModuleDescriptor, ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist>()
                {
                    @Override
                    public ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist apply(ConnectWebFragmentLocationBlacklistModuleDescriptor connectWebFragmentLocationBlacklistModuleDescriptor)
                    {
                        return connectWebFragmentLocationBlacklistModuleDescriptor.getModule();
                    }
                })
                .map(new Function<ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist, ImmutableSet<String>>() {
                    @Override
                    public ImmutableSet<String> apply(ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist connectWebFragmentLocationBlacklist)
                    {
                        return connectWebFragmentLocationBlacklist.getWebItemBlacklistedLocations();
                    }
                })
                .flatMap(new Function<ImmutableSet<String>, Stream<String>>()
                {
                    @Override
                    public Stream<String> apply(ImmutableSet<String> strings)
                    {
                        return strings.stream();
                    }
                })
                .collect(Collectors.toSet());
    }

}
