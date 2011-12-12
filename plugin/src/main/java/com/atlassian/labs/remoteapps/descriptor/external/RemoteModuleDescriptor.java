package com.atlassian.labs.remoteapps.descriptor.external;

import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

/**
 *
 */
public class RemoteModuleDescriptor extends AbstractModuleDescriptor<RemoteModuleGenerator>
{
    private final ModuleFactory moduleFactory;
    private volatile Schema schema;
    private Element element;

    public RemoteModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        this.moduleFactory = moduleFactory;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element
    ) throws PluginParseException
    {
        super.init(plugin, element);
        Element e = element.element("schema");
        if (e == null)
        {
            throw new PluginParseException("Missing schema element");
        }
        this.element = element;
    }

    @Override
    public void enabled()
    {
        super.enabled();
        final Element e = element.element("schema");
        String schemaClassName = e.attributeValue("class");
        if (schemaClassName != null)
        {
            Class<? extends Schema> schemaClass = null;
            try
            {
                schemaClass = getPlugin().loadClass(schemaClassName, null);
            }
            catch (ClassNotFoundException e1)
            {
                throw new PluginParseException("Cannot find schema class '" + schemaClassName + "'", e1);
            }
            this.schema = ((ContainerManagedPlugin)getPlugin()).getContainerAccessor().createBean(schemaClass);
        }
        else
        {
            this.schema = new StaticSchema(
                getPlugin(),
                e.attributeValue("id"),
                e.attributeValue("path"),
                e.attributeValue("type"),
                e.attributeValue("maxOccurs", "unbounded")
            );
        }
    }

    @Override
    public RemoteModuleGenerator getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }

    public Schema getSchema()
    {
        return schema;
    }
}
