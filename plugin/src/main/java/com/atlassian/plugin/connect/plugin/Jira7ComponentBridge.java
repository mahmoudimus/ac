package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.module.ContainerAccessor;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Field;

/**
 * This class is full of OSGI/Spring hacks to Conditionally ComponentImport the UserPropertyService from JIRA
 * TODO: This class should be removed when JIRA 7 will hit OD with a simple ComponentImport in JiraImports class
 * This requires the JIRA api pom dependency to be bumped to JIRA 7
 */
@ExportAsService
@JiraComponent
public class Jira7ComponentBridge implements LifecycleAware
{
    private static final Logger logger = LoggerFactory.getLogger(Jira7ComponentBridge.class);
    private final ContainerManagedPlugin theConnectPlugin;

    private BundleContext bundleContext;

    @Autowired
    public Jira7ComponentBridge(final BundleContext bundleContext,  PluginRetrievalService pluginRetrievalService)
    {
        this.bundleContext = bundleContext;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
    }

    @Override
    public void onStart()
    {
        // service tracker to respond to changes in service availability; will invoke addingService immediately if already present
        new ServiceTracker<>(bundleContext, "com.atlassian.jira.bc.user.UserPropertyService", new ServiceTrackerCustomizer<Object, Object>()
        {
            @Override
            public Object addingService(final ServiceReference<Object> reference)
            {
                logger.debug("addingService [{}]", reference);
                ContainerAccessor containerAccessor = theConnectPlugin.getContainerAccessor();

                try
                {
                    Field f = containerAccessor.getClass().getDeclaredField("nativeBeanFactory");
                    f.setAccessible(true);
                    SingletonBeanRegistry registry = (SingletonBeanRegistry) f.get(containerAccessor);
                    registry.registerSingleton("userPropertyService", bundleContext.getService(reference));
                }
                catch (Exception e)
                {
                    logger.info("Tried to register dynamically the UserPropertyService but failed.");
                }

                return bundleContext.getService(reference);
            }

            @Override
            public void modifiedService(final ServiceReference<Object> reference, final Object service)
            {
                // nothing to do here
            }

            @Override
            public void removedService(final ServiceReference<Object> reference, final Object service)
            {
                logger.debug("removedService [{}] [{}]", reference, service);
                bundleContext.ungetService(reference);
            }
        }).open();
    }
}
