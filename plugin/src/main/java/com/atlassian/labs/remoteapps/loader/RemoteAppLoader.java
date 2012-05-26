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
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.atlassian.labs.remoteapps.util.BundleUtil.findBundleForPlugin;
import static com.atlassian.labs.remoteapps.util.RemoteAppManifestReader.isRemoteApp;
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
    private final BundleContext bundleContext;
    private final AggregateModuleDescriptorFactory aggregateModuleDescriptorFactory;
    private final PluginEventManager pluginEventManager;

    private final Map<String,Iterable<RemoteModule>> remoteModulesByApp;
    private static final Logger log = LoggerFactory.getLogger(
            RemoteAppLoader.class);

    @Autowired
    public RemoteAppLoader(DescriptorValidator descriptorValidator,
            ModuleGeneratorManager moduleGeneratorManager, PluginAccessor pluginAccessor,
            StartableForPlugins startableForPlugins, EventPublisher eventPublisher,
            BundleContext bundleContext,
            AggregateModuleDescriptorFactory aggregateModuleDescriptorFactory,
            PluginEventManager pluginEventManager)
    {
        this.descriptorValidator = descriptorValidator;
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.pluginAccessor = pluginAccessor;
        this.startableForPlugins = startableForPlugins;
        this.eventPublisher = eventPublisher;
        this.bundleContext = bundleContext;
        this.aggregateModuleDescriptorFactory = aggregateModuleDescriptorFactory;
        this.pluginEventManager = pluginEventManager;
        this.remoteModulesByApp = new ConcurrentHashMap<String,Iterable<RemoteModule>>();
        this.pluginEventManager.register(this);
    }

    /*!
    The Remote Apps loading process contains the steps the Remote Apps framework goes through when
    loading, or enabling a Remote App.  This process happens every time the underlying Remote App
    plugin is enabled, which could be:

    1. After the Remote App is installed
    2. When the Atlassian application is started
    3. After the Atlassian administrator clicks the "Enable" button in the plugin manager UI

    The loading process operates on the underlying plugin bundle that each Remote Apps is converted
    as part of the installation.  This can be triggered via:

    1. The deprecated `remote-app` plugin module - The deprecated remote-app plugin module can kick
    the loading process, however, it isn't recommended as any failures during start up will not
    result in the plugin being marked as disabled, and therefore, errors won't be clearly visible
    to the administrator.  This module type is only left in to support legacy Remote Apps.
    2. A `META-INF/spring/remoteapps-loader.xml` file.  This file is made part of the installed plugin
    in order to consume a service from the Remote Apps plugin and call init() on it.  The init()
    method, in turn, starts this process.
     */
    public void load(Bundle bundle, Document appDescriptor) throws Exception
    {
        final Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));
        try
        {
            /*!
            ### Step 1 - Validation

            The first step in the loading process is validation.  Validation is necessary here
            because a Remote App could have been installed as a normal Atlassian plugin or OSGi
            bundle, and in that case, we need to ensure its configuration is correct.

            As part of validation, the loading process will wait until all modules necessary for
            this Remote App to load are present.  This includes the required remote modules defined
            locally or via OSGi services as well as any waiting that each remote module needs to
            do before it can successfully process this Remote App.

            If any modules aren't present, the loading process will wait 20 seconds for them, but
            if still not available, an exception will be thrown halting the process.
             */
            moduleGeneratorManager.waitForModules(appDescriptor.getRootElement());

            /*!
            Once the modules are all present, the `atlassian-remote-app.xml` XML descriptor from the
            bundle will validated.  This validation involves processing the descriptor against the
            generated XML Schema for this Atlassian application instance.
             */
            descriptorValidator.validate(URI.create("atlassian-remote-app.xml"), appDescriptor);

            /*!
            ### Step 2 - Remote Module Registration

            With the descriptor validated, the next step is to process the descriptor to generate
            the remote modules.  Each remote module has the opportunity to provide one or more
            Atlassian plugin ModuleDescriptor instances that will be exposed as OSGi services,
            thereby attaching the plugin modules to the Remote App plugin instance.

            For example, a generate-page module will generate and register the following Atlassian
            plugin module instances:

            1. `servlet` - This plugin servlet provides an addressable that is decorated as a normal
            page and embeds an iframe
            2. `web-item` - The web item inserts a link to the general page in the Atlassian
            application UI

            The first module to be processed will always be the root element, treated as input for
            the Atlassian Application Links module.  This module will generate the application link
            that formally associates the Remote App to the Atlassian application.
            */

            final RemoteAppCreationContext firstContext = new DefaultRemoteAppCreationContext(plugin, aggregateModuleDescriptorFactory, bundle, null);

            final List<RemoteModule> remoteModules = generateRemoteModules(bundle, appDescriptor,
                    plugin, firstContext);
            registerDescriptors(bundle, plugin, remoteModules);

            /*!
            ### Step 3 - Startable registration

            Not all remote module code can execute while the plugin is still be enabled, so modules
            that implement StartableRemoteModule can register to be called once both the plugin is
            enabled and the Atlassian app has been fully started.  For example, the oauth module
            uses this callback to associate the Remote App as an OAuth consumer and provider.
             */
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
                    /*!
                    Once all the startable remote modules have executed and both the plugin and
                    the Atlassian application have fully started, the RemoteAppStartedEvent will be
                    fired.  This event will be published as the webhook `remote\_app\_started`.
                     */
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

    /*!
    ## Uninstallation

    Whenever a Remote App's plugin has been explicitly disabled by an Atlassian administrator,
    the PluginDisabledEvent is fired.  Remote Apps listens to this event to try to clean up
    any state for installed Remote Apps, as it treats a disabled Remote App as an uninstalled one.

    An example of uninstallation code is the oauth module.  On uninstallation, it removes the OAuth
    consumer and provider registration for the Remote App.
     */
    @PluginEventListener
    public void onPluginDisabled(PluginDisabledEvent event)
    {
        String pluginKey = event.getPlugin().getKey();
        Bundle bundle = findBundleForPlugin(bundleContext, pluginKey);
        if (bundle != null && isRemoteApp(bundle))
        {
            for (RemoteModuleGenerator generator : moduleGeneratorManager.getRemoteModuleGenerators())
            {
                if (generator instanceof UninstallableRemoteModuleGenerator)
                {
                    ((UninstallableRemoteModuleGenerator)generator).uninstall(pluginKey);
                }
            }
        }
    }
    /*!-helper methods*/

    private List<RemoteModule> generateRemoteModules(Bundle bundle, Document appDescriptor,
            Plugin plugin, RemoteAppCreationContext firstContext)
    {
        final List<RemoteModule> remoteModules = newArrayList();
        ApplicationTypeModule module = (ApplicationTypeModule) moduleGeneratorManager.getApplicationTypeModuleGenerator().generate(firstContext, appDescriptor.getRootElement());

        remoteModules.add(module);

        final RemoteAppCreationContext childContext = new DefaultRemoteAppCreationContext(plugin, aggregateModuleDescriptorFactory, bundle, module.getApplicationType());

        moduleGeneratorManager.processDescriptor(appDescriptor.getRootElement(),
                new ModuleGeneratorManager.ModuleHandler()
                {
                    @Override
                    public void handle(Element element, RemoteModuleGenerator generator)
                    {
                        log.info("Registering module '" + generator.getType() + "' for key" +
                                " '" + element.attributeValue(
                                "key") + "'");
                        remoteModules.add(generator.generate(childContext, element));
                    }
                });
        remoteModulesByApp.put(plugin.getKey(), remoteModules);
        return remoteModules;
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
        pluginEventManager.unregister(this);
    }
}
