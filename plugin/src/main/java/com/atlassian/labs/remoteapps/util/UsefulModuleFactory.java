package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ClassPrefixModuleFactory;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
import com.atlassian.plugin.osgi.module.BeanPrefixModuleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Sets.newHashSet;

/**
 *
 */
@Component
public class UsefulModuleFactory implements ModuleFactory
{
    private final PrefixDelegatingModuleFactory delegate;

    @Autowired
    public UsefulModuleFactory(HostContainer hostContainer)
    {
        this.delegate =new PrefixDelegatingModuleFactory(newHashSet(
                new BeanPrefixModuleFactory(),
                new ClassPrefixModuleFactory(hostContainer)));;
    }

    @Override
    public <T> T createModule(String s, ModuleDescriptor<T> tModuleDescriptor) throws PluginParseException
    {
        return delegate.createModule(s, tModuleDescriptor);
    }
}
