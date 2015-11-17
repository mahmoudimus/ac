package com.atlassian.plugin.connect.plugin.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;

public abstract class AbstractPluggableModuleExtractor<T> {

    private final PluginAccessor pluginAccessor;
    private final Class<? extends ModuleDescriptor<? extends T>> moduleClass;

    public AbstractPluggableModuleExtractor(PluginAccessor pluginAccessor, Class<? extends ModuleDescriptor<? extends T>> moduleClass) {
        this.pluginAccessor = pluginAccessor;
        this.moduleClass = moduleClass;
    }

    public Iterable<T> getModules()
    {
        return pluginAccessor.getModules(new ModuleDescriptorOfClassPredicate<>(moduleClass));
    }
}
