package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.integration.plugins.DescribedModuleDescriptorFactory;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTracker;
import com.atlassian.labs.remoteapps.util.tracker.WaitableServiceTrackerFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Provides information to support atlassian-plugin.xml validation
 */
@Component
public class PluginDescriptorValidatorProvider implements DescriptorValidatorProvider
{
    private final Plugin plugin;
    private final ApplicationProperties applicationProperties;
    private WaitableServiceTracker<String, DescribedModuleDescriptorFactory> describedModuleFactories;

    @Autowired
    public PluginDescriptorValidatorProvider(PluginRetrievalService pluginRetrievalService,
            ApplicationProperties applicationProperties,
            WaitableServiceTrackerFactory waitableServiceTrackerFactory)
    {
        this.applicationProperties = applicationProperties;
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
        return applicationProperties.getBaseUrl() + "/rest/remoteapps/1/installer/schema/atlassian-plugin";
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
    public URL getSchemaUrl()
    {
        return plugin.getResource("/xsd/atlassian-plugin.xsd");
    }
}
