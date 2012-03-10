package com.atlassian.labs.remoteapps.loader;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.DescriptorValidator;
import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.event.RemoteAppStartFailedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStoppedEvent;
import com.atlassian.labs.remoteapps.modules.DefaultRemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModule;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Loads the remote app from its descriptor
 */
@Component
public class RemoteAppLoader implements DisposableBean
{
    private final DescriptorValidator descriptorValidator;
    private final ModuleGeneratorManager moduleGeneratorManager;
    private final PluginAccessor pluginAccessor;
    private final StartableForPlugins startableForPlugins;
    private final EventPublisher eventPublisher;
    private final AggregateModuleDescriptorFactory aggregateModuleDescriptorFactory;

    private final Map<String,Iterable<RemoteModule>> remoteModulesByApp;
    private static final Logger log = LoggerFactory.getLogger(
            RemoteAppLoader.class);

    @Autowired
    public RemoteAppLoader(DescriptorValidator descriptorValidator,
            ModuleGeneratorManager moduleGeneratorManager, PluginAccessor pluginAccessor,
            StartableForPlugins startableForPlugins, EventPublisher eventPublisher,
            AggregateModuleDescriptorFactory aggregateModuleDescriptorFactory)
    {
        this.descriptorValidator = descriptorValidator;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.pluginAccessor = pluginAccessor;
        this.startableForPlugins = startableForPlugins;
        this.eventPublisher = eventPublisher;
        this.aggregateModuleDescriptorFactory = aggregateModuleDescriptorFactory;
        this.remoteModulesByApp = new ConcurrentHashMap<String,Iterable<RemoteModule>>();
        this.eventPublisher.register(this);
    }

    @EventListener
    public void onPluginUninstall(PluginUninstalledEvent event)
    {
        for (RemoteModuleGenerator generator : moduleGeneratorManager.getRemoteModuleGenerators())
        {
            if (generator instanceof UninstallableRemoteModuleGenerator)
            {
                ((UninstallableRemoteModuleGenerator)generator).uninstall(event.getPlugin().getKey());
            }
        }
    }

    public void load(Bundle bundle, Document appDescriptor) throws Exception
    {
        final Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
        try
        {
            // waits for modules to be loaded and anything they need to wait for
            moduleGeneratorManager.waitForModules(appDescriptor.getRootElement());
            
            descriptorValidator.validate("atlassian-remote-app.xml", appDescriptor);

            final RemoteAppCreationContext firstContext = new DefaultRemoteAppCreationContext(plugin, aggregateModuleDescriptorFactory, bundle, null);
            ApplicationTypeModule module = (ApplicationTypeModule) moduleGeneratorManager.getApplicationTypeModuleGenerator().generate(firstContext, appDescriptor.getRootElement());
            final List<RemoteModule> remoteModules = newArrayList();
            remoteModules.add(module);

            final RemoteAppCreationContext childContext = new DefaultRemoteAppCreationContext(plugin, aggregateModuleDescriptorFactory, bundle, module.getApplicationType());

            moduleGeneratorManager.processDescriptor(appDescriptor.getRootElement(),
                    new ModuleGeneratorManager.ModuleHandler()
                    {
                        @Override
                        public void handle(Element element, RemoteModuleGenerator generator)
                        {
                            log.info(
                                    "Registering module '" + generator.getType() + "' for key" +
                                            " '" + element.attributeValue(
                                            "key") + "'");
                            remoteModules.add(generator.generate(childContext, element));
                        }
                    });
            remoteModulesByApp.put(plugin.getKey(), remoteModules);
            registerDescriptors(bundle, plugin, remoteModules);
            startableForPlugins.register(plugin.getKey(), new Runnable()
            {

                @Override
                public void run()
                {
                    for (final RemoteModule remoteModule : remoteModules)
                    {
                        if (remoteModule instanceof StartableRemoteModule)
                        {
                            startableForPlugins.register(plugin.getKey(), new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    ((StartableRemoteModule)remoteModule).start();
                                }
                            });
                        }
                    }
                    eventPublisher.publish(new RemoteAppStartedEvent(plugin.getKey()));
                    log.info("Remote app '{}' started successfully", plugin.getKey());
                }
            });
        }
        catch (final Exception e)
        {
            eventPublisher.publish(new RemoteAppStartFailedEvent(plugin.getKey(), e));
            log.info("Remote app '{}' failed to start: {}", plugin.getKey(), e.getMessage());
            throw e;
        }
    }
    
    private void registerDescriptors(Bundle bundle, Plugin plugin, Iterable<RemoteModule> modules)
    {
        BundleContext targetBundleContext = bundle.getBundleContext();
        for (RemoteModule module : modules)
        {
            for (ModuleDescriptor descriptor : module.getModuleDescriptors())
            {
                if (plugin.getModuleDescriptor(descriptor.getKey()) != null)
                {
                    log.error("Duplicate key '" + descriptor.getKey() + "' detected, skipping");
                }
                else
                {
                    targetBundleContext.registerService(ModuleDescriptor.class.getName(),
                            descriptor, null);
                }
            }
        }
    }

    public void unload(Bundle bundle)
    {
        String pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
        Iterable<RemoteModule> modules = remoteModulesByApp.remove(pluginKey);
        if (modules != null)
        {
            for (RemoteModule module : modules)
            {
                if (module instanceof ClosableRemoteModule)
                {
                    ((ClosableRemoteModule)module).close();
                }
            }
        }
        startableForPlugins.unregister(pluginKey);
        eventPublisher.publish(new RemoteAppStoppedEvent(pluginKey));
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
