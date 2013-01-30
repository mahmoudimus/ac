package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.osgi.tracker.WaitableServiceTracker;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.plugin.PermissionManager;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.schema.descriptor.DescribedModuleDescriptorFactory;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.*;

/**
 * Provides information to support atlassian-plugin.xml validation
 */
@Component
public class PluginDescriptorValidatorProvider implements DescriptorValidatorProvider
{
    private final Plugin plugin;
    private final ApplicationProperties applicationProperties;
    private final PermissionsReader permissionsReader;
    private WaitableServiceTracker<String, DescribedModuleDescriptorFactory> describedModuleFactories;

    @Autowired
    public PluginDescriptorValidatorProvider(PluginRetrievalService pluginRetrievalService,
                                             ApplicationProperties applicationProperties,
                                             WaitableServiceTrackerFactory waitableServiceTrackerFactory,
                                             PermissionsReader permissionsReader
    )
    {
        this.applicationProperties = applicationProperties;
        this.permissionsReader = permissionsReader;
        this.plugin = pluginRetrievalService.getPlugin();
        this.describedModuleFactories = waitableServiceTrackerFactory.create(
                DescribedModuleDescriptorFactory.class,
                new Function<DescribedModuleDescriptorFactory, String>()
                {
                    @Override
                    public String apply(DescribedModuleDescriptorFactory input)
                    {
                        return String.valueOf(System.identityHashCode(input));
                    }
                });
    }
    @Override
    public String getSchemaNamespace()
    {
        return applicationProperties.getBaseUrl() + "/rest/remotable-plugins/1/installer/schema/atlassian-plugin";
    }

    @Override
    public String getRootElementName()
    {
        return "AtlassianPluginType";
    }

    @Override
    public Iterable<Schema> getModuleSchemas()
    {
        Set<Schema> schemas = newHashSet();
        for (DescribedModuleDescriptorFactory factory : describedModuleFactories.getAll())
        {
            for (String key : factory.getModuleDescriptorKeys())
            {
                schemas.add(factory.getSchema(key));
            }
        }
        return schemas;
    }

    @Override
    public void performSecondaryValidations(Document document) throws InstallationFailedException
    {
        Set<String> permissions = permissionsReader.readPermissionsFromDescriptor(document, InstallationMode.REMOTE);
        Collection<String> moduleTypes = transform((List<Element>)document.getRootElement().elements(), new Function<Element,String>()
        {
            @Override
            public String apply(Element input)
            {
                return input.getName();
            }
        });
        for (DescribedModuleDescriptorFactory factory : describedModuleFactories.getAll())
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
