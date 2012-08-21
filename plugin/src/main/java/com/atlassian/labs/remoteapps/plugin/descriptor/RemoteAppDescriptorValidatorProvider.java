package com.atlassian.labs.remoteapps.plugin.descriptor;

import com.atlassian.labs.remoteapps.plugin.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.spi.InstallationFailedException;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * Provides information to support validation of atlassian-remote-app.xml
 */
@Component
public class RemoteAppDescriptorValidatorProvider implements DescriptorValidatorProvider
{
    private final Plugin plugin;
    private final ApplicationProperties applicationProperties;
    private final ModuleGeneratorManager moduleGeneratorManager;

    @Autowired
    public RemoteAppDescriptorValidatorProvider(PluginRetrievalService pluginRetrievalService,
            ApplicationProperties applicationProperties,
            ModuleGeneratorManager moduleGeneratorManager)
    {
        this.applicationProperties = applicationProperties;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.plugin = pluginRetrievalService.getPlugin();
    }
    @Override
    public String getSchemaNamespace()
    {
        return applicationProperties.getBaseUrl() + "/rest/remoteapps/1/installer/schema/remote-app";
    }

    @Override
    public String getRootElementName()
    {
        return "RemoteAppType";
    }

    @Override
    public Iterable<Schema> getModuleSchemas()
    {
        return Iterables.transform(moduleGeneratorManager.getAllValidatableGenerators(),
                new Function<RemoteModuleGenerator, Schema>()
                {
                    @Override
                    public Schema apply(RemoteModuleGenerator input)
                    {
                        return input.getSchema();
                    }
                });
    }

    @Override
    public void performSecondaryValidations(Document document) throws InstallationFailedException
    {
        // nothing to do here as only safe modules are available
    }

    @Override
    public URL getSchemaUrl()
    {
        return plugin.getResource("/xsd/remote-app.xsd");
    }
}
