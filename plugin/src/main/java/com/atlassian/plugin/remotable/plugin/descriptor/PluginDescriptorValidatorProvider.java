package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.RequirePermission;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.remotable.spi.product.ProductAccessor;
import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.remotable.plugin.rest.InstallerResource.ATLASSIAN_PLUGIN_REMOTABLE_SCHEMA_PATH;
import static com.atlassian.plugin.remotable.plugin.rest.InstallerResource.ATLASSIAN_PLUGIN_SCHEMA_PATH;
import static com.atlassian.plugin.remotable.plugin.rest.InstallerResource.INSTALLER_RESOURCE_PATH;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;

/**
 * Provides information to support atlassian-plugin.xml validation
 */
@Component
public class PluginDescriptorValidatorProvider implements DescriptorValidatorProvider
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Plugin plugin;
    private final ApplicationProperties applicationProperties;
    private final DescribedModuleDescriptorFactoryAccessor describedModuleDescriptorFactoryAccessor;
    private final PermissionsReader permissionsReader;
    private final ProductAccessor productAccessor;

    @Autowired
    public PluginDescriptorValidatorProvider(PluginRetrievalService pluginRetrievalService,
                                             ApplicationProperties applicationProperties,
                                             DescribedModuleDescriptorFactoryAccessor describedModuleDescriptorFactoryAccessor,
                                             PermissionsReader permissionsReader, ProductAccessor productAccessor)
    {
        this.plugin = checkNotNull(pluginRetrievalService).getPlugin();
        this.applicationProperties = checkNotNull(applicationProperties);
        this.describedModuleDescriptorFactoryAccessor = checkNotNull(describedModuleDescriptorFactoryAccessor);
        this.permissionsReader = checkNotNull(permissionsReader);
        this.productAccessor = checkNotNull(productAccessor);
    }

    @Override
    public String getSchemaNamespace(InstallationMode installationMode)
    {
        return format("%s/rest/remotable-plugins/1%s%s",
                applicationProperties.getBaseUrl(),
                INSTALLER_RESOURCE_PATH,
                installationMode == InstallationMode.REMOTE ? ATLASSIAN_PLUGIN_REMOTABLE_SCHEMA_PATH : ATLASSIAN_PLUGIN_SCHEMA_PATH);
    }

    @Override
    public String getRootElementName()
    {
        return "AtlassianPluginType";
    }

    @Override
    public Iterable<Schema> getModuleSchemas(final InstallationMode mode)
    {
        return filter(concat(transform(describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories(),
                new Function<DescribedModuleDescriptorFactory, Iterable<Schema>>()
                {
                    @Override
                    public Iterable<Schema> apply(final DescribedModuleDescriptorFactory factory)
                    {
                        return getModuleSchemas(factory, mode);
                    }
                })),
                notNull());
    }

    private Iterable<Schema> getModuleSchemas(final DescribedModuleDescriptorFactory factory, final InstallationMode mode)
    {
        return transform(factory.getModuleDescriptorKeys(), new Function<String, Schema>()
        {
            @Override
            public Schema apply(String key)
            {
                return getModuleSchema(factory, mode, key);
            }
        });
    }

    private Schema getModuleSchema(DescribedModuleDescriptorFactory factory, InstallationMode mode, String key)
    {
        final Set<String> allowedPermissions = productAccessor.getAllowedPermissions(mode);
        if (allowedPermissions.isEmpty())
        {
            return factory.getSchema(key);
        }

        // a little bit of reflection for those still on Atlassian Plugins 2.x and not yet on 3+
        if (!hasRequiredPermissionsAnnotation())
        {
            return factory.getSchema(key);
        }

        final Class<? extends ModuleDescriptor> moduleDescriptorClass = factory.getModuleDescriptorClass(key);
        if (!hasRequiredPermissions(moduleDescriptorClass))
        {
            return factory.getSchema(key);
        }

        final Set<String> requiredPermissions = getRequiredPermissions(moduleDescriptorClass);
        if (Sets.difference(requiredPermissions, allowedPermissions).isEmpty())
        {
            return factory.getSchema(key);
        }

        return null;
    }

    private boolean hasRequiredPermissionsAnnotation()
    {
        try
        {
            final Class<RequirePermission> requirePermissionClass = RequirePermission.class;
            return requirePermissionClass != null;
        }
        catch (NoClassDefFoundError e)
        {
            logger.debug("Couldn't find annotation RequirePermission. Probably because we're still on Atlassian Plugins 2.", e);
            return false;
        }
    }

    private boolean hasRequiredPermissions(Class<? extends ModuleDescriptor> moduleDescriptorClass)
    {
        return moduleDescriptorClass.isAnnotationPresent(RequirePermission.class);
    }

    private Set<String> getRequiredPermissions(Class<? extends ModuleDescriptor> moduleDescriptorClass)
    {
        return copyOf(moduleDescriptorClass.getAnnotation(RequirePermission.class).value());
    }

    @Override
    public void performSecondaryValidations(Document document) throws InstallationFailedException
    {
        Set<String> permissions = permissionsReader.readPermissionsFromDescriptor(document, InstallationMode.REMOTE);
        Collection<String> moduleTypes = transform((List<Element>) document.getRootElement().elements(), new Function<Element, String>()
        {
            @Override
            public String apply(Element input)
            {
                return input.getName();
            }
        });
        for (DescribedModuleDescriptorFactory factory : describedModuleDescriptorFactoryAccessor.getDescribedModuleDescriptorFactories())
        {
            for (String key : factory.getModuleDescriptorKeys())
            {
                if (moduleTypes.contains(key))
                {
                    for (String permission : factory.getSchema(key).getRequiredPermissions())
                    {
                        if (!permissions.contains(permission))
                        {
                            throw new InstallationFailedException("The permission '" + permission + " is required " +
                                    "to use the '" + key + "' module type");
                        }
                    }
                }
            }
        }

        // todo: ensure any non-safe permissions are not requested, even in dogfooding mode
    }

    @Override
    public URL getSchemaUrl()
    {
        return plugin.getResource("/xsd/atlassian-plugin.xsd");
    }
}