package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.remotable.spi.descriptor.DescribedModuleDescriptorFactory;
import com.atlassian.plugin.remotable.spi.schema.Schema;
import com.atlassian.plugin.remotable.spi.schema.SchemaFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;

import java.util.Collections;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Described module descriptor factory for internal use
 */
class DefaultDescribedModuleDescriptorFactory<T extends ModuleDescriptor<?>> implements DescribedModuleDescriptorFactory
{

    private final String type;
    private final Iterable<String> typeList;
    private final HostContainer hostContainer;
    private final Class<T> moduleDescriptorClass;
    private final SchemaFactory schemaFactory;

    /**
     * Constructs an instance using a specific host container
     *
     * @param type The type of module
     * @param moduleDescriptorClass The descriptor class
     * @param schemaFactory
     * @since 2.2.0
     */
    DefaultDescribedModuleDescriptorFactory(HostContainer hostContainer,
            final String type,
            final Class<T> moduleDescriptorClass, SchemaFactory schemaFactory)
    {
        this.hostContainer = hostContainer;
        this.moduleDescriptorClass = moduleDescriptorClass;
        this.type = type;
        this.schemaFactory = schemaFactory;
        this.typeList = singleton(type);
    }

    public ModuleDescriptor getModuleDescriptor(final String type) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        T result = null;
        if (this.type.equals(type))
        {
            result = (T) hostContainer.create(moduleDescriptorClass);
        }
        return result;
    }

    public boolean hasModuleDescriptor(final String type)
    {
        return (this.type.equals(type));
    }

    @Override
    public Iterable<String> getModuleDescriptorKeys()
    {
        return typeList;
    }

    @Override
    public Schema getSchema(String type)
    {
        return (this.type.equals(type) ? schemaFactory.getSchema() : null);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ModuleDescriptor<?>> getModuleDescriptorClass(final String type)
    {
        return (this.type.equals(type) ? moduleDescriptorClass : null);
    }

    @SuppressWarnings("unchecked")
    public Set<Class<ModuleDescriptor<?>>> getModuleDescriptorClasses()
    {
        return Collections.singleton((Class<ModuleDescriptor<?>>) moduleDescriptorClass);
    }
}
