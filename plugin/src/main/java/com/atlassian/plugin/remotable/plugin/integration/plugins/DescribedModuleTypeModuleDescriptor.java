package com.atlassian.plugin.remotable.plugin.integration.plugins;

import com.atlassian.plugin.remotable.spi.descriptor.DescribedModuleDescriptorFactory;
import com.atlassian.plugin.remotable.spi.schema.DocumentBasedSchema;
import com.atlassian.plugin.remotable.spi.schema.Schema;
import com.atlassian.plugin.remotable.spi.schema.SchemaFactory;
import com.atlassian.plugin.remotable.spi.schema.SchemaTransformer;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.descriptors.CannotDisable;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.external.ListableModuleDescriptorFactory;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.annotation.Nullable;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

/**
 * Descriptor that allows described module descriptor factories to be configured in XML.  Main value
 * is the ability to reuse the name and description of the module descriptor configuration.
 */
@CannotDisable
public class DescribedModuleTypeModuleDescriptor extends AbstractModuleDescriptor<DescribedModuleDescriptorFactory>
{
    private static final String[] PUBLIC_INTERFACES = new String[] {
            ModuleDescriptorFactory.class.getName(),
            ListableModuleDescriptorFactory.class.getName(),
            DescribedModuleDescriptorFactory.class.getName()
    };

    private final HostContainer hostContainer;
    private final BundleContext bundleContext;
    private String schemaFactoryClassName;
    private String type;
    private String schemaTransformerClassName;
    private String maxOccurs;
    private Iterable<String> requiredPermissions;
    private Iterable<String> optionalPermissions;


    public DescribedModuleTypeModuleDescriptor(HostContainer hostContainer, BundleContext bundleContext)
    {
        super(ModuleFactory.LEGACY_MODULE_FACTORY);
        this.hostContainer = hostContainer;
        this.bundleContext = bundleContext;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.type = getOptionalAttribute(element, "type", getKey());
        this.schemaFactoryClassName = getOptionalAttribute(element, "schema-factory-class", null);
        this.schemaTransformerClassName = getOptionalAttribute(element, "schema-transformer-class", null);
        this.maxOccurs = getOptionalAttribute(element, "max-occurs", "unbounded");
        this.requiredPermissions = getPermissions(element.element("required-permissions"));
        this.optionalPermissions = getPermissions(element.element("optional-permissions"));
    }

    private Iterable<String> getPermissions(Element element)
    {
        if (element != null)
        {
            return Lists.transform(element.elements("permission"), new Function<Element,String>()
            {
                @Override
                public String apply(@Nullable Element input)
                {
                    return input.getTextTrim();
                }
            });
        }
        return emptyList();
    }

    @Override
    public void enabled()
    {
        super.enabled();
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, getPluginKey());
        checkNotNull(bundle, "Cannot find bundle for plugin " + getPluginKey());

        SchemaTransformer schemaTransformer = schemaTransformerClassName != null
                ? hostContainer.create(findClass(schemaTransformerClassName, SchemaTransformer.class))
                : SchemaTransformer.IDENTITY;
        SchemaFactory schemaFactory = schemaFactoryClassName != null
                ? hostContainer.create(findClass(schemaFactoryClassName, SchemaFactory.class))
                : buildSingleton(DocumentBasedSchema.builder(type)
                                                    .setPlugin(getPlugin())
                                                    .setName(getName() != null ? getName() : getKey())
                                                    .setDescription(getDescription() != null ? getDescription() : "")
                                                    .setTransformer(schemaTransformer)
                                                    .setMaxOccurs(maxOccurs)
                                                    .setRequiredPermissions(requiredPermissions)
                                                    .setOptionalPermissions(optionalPermissions)
                                                    .build());

        DescribedModuleDescriptorFactory factory = new DefaultDescribedModuleDescriptorFactory(hostContainer, type,
                findClass(moduleClassName, ModuleDescriptor.class), schemaFactory);
        bundle.getBundleContext().registerService(PUBLIC_INTERFACES, factory, null);
    }

    private <T> Class<? extends T> findClass(String className, Class<T> castTo)
    {
        checkNotNull(className);
        Class<T> clazz = null;
        try
        {
            clazz = plugin.loadClass(className, getClass());
        }
        catch (ClassNotFoundException e)
        {
            throw new PluginParseException("Unable to find class " + className);
        }
        return clazz.asSubclass(castTo);
    }

    @Override
    public DescribedModuleDescriptorFactory getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }

    private SchemaFactory buildSingleton(final Schema schema)
    {
        return new SchemaFactory()
        {
            @Override
            public Schema getSchema()
            {
                return schema;
            }
        };
    }

}
