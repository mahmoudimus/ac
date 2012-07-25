package com.atlassian.labs.remoteapps.apputils.ao;

import com.atlassian.activeobjects.config.ActiveObjectsConfiguration;
import com.atlassian.activeobjects.config.ActiveObjectsConfigurationFactory;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.java.ao.RawEntity;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.*;

public final class ActiveObjectsConfigurationFactoryBean implements FactoryBean, DisposableBean
{
    private final Supplier<ServiceRegistration> registration;
    private final Supplier<ActiveObjectsConfiguration> configuration;

    public ActiveObjectsConfigurationFactoryBean(final BundleContext bundleContext, final ActiveObjectsConfigurationFactory factory)
    {
        this.configuration = memoize(synchronizedSupplier(new ActiveObjectsConfigurationSupplier(bundleContext, factory)));
        this.registration = memoize(synchronizedSupplier(new ActiveObjectConfigurationServiceRegistrationSupplier(bundleContext, configuration)));
    }

    @Override
    public Object getObject() throws Exception
    {
        // this creates the configuration and registers it with the OSGi context
        registration.get();

        return configuration.get();
    }

    @Override
    public Class getObjectType()
    {
        return ActiveObjectsConfiguration.class;
    }

    @Override
    public void destroy() throws Exception
    {
        registration.get().unregister();
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    private static class ActiveObjectsConfigurationSupplier implements Supplier<ActiveObjectsConfiguration>
    {
        private final BundleContext bundleContext;
        private final ActiveObjectsConfigurationFactory factory;

        public ActiveObjectsConfigurationSupplier(BundleContext bundleContext, ActiveObjectsConfigurationFactory factory)
        {
            this.bundleContext = checkNotNull(bundleContext);
            this.factory = checkNotNull(factory);
        }

        @Override
        public ActiveObjectsConfiguration get()
        {
            final Bundle bundle = bundleContext.getBundle();
            return factory.getConfiguration(bundle, bundle.getSymbolicName(), getEntities(), getUpgradeTasks());
        }

        public Set<Class<? extends RawEntity<?>>> getEntities()
        {
            // not typing the iterable here, because of the cast afterward, which wouldn't compile otherwise!
            final Iterable entityClasses =
                    new BundleContextScanner().findClasses(
                            bundleContext,
                            "ao.model",
                            new LoadClassFromBundleFunction(bundleContext.getBundle()),
                            new IsAoEntityPredicate()
                    );

            @SuppressWarnings("unchecked") // we're filtering to get what we want!
            final Iterable<Class<? extends RawEntity<?>>> entities = (Iterable<Class<? extends RawEntity<?>>>) entityClasses;
            return ImmutableSet.copyOf(entities);
        }

        public List<ActiveObjectsUpgradeTask> getUpgradeTasks()
        {
            return Lists.newArrayList();
        }
    }

    private static class ActiveObjectConfigurationServiceRegistrationSupplier implements Supplier<ServiceRegistration>
    {
        private final BundleContext bundleContext;
        private final Supplier<ActiveObjectsConfiguration> configuration;

        public ActiveObjectConfigurationServiceRegistrationSupplier(BundleContext bundleContext, Supplier<ActiveObjectsConfiguration> configuration)
        {
            this.bundleContext = checkNotNull(bundleContext);
            this.configuration = checkNotNull(configuration);
        }

        @Override
        public ServiceRegistration get()
        {
            final Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put("com.atlassian.plugin.key", bundleContext.getBundle().getSymbolicName());
            return bundleContext.registerService(ActiveObjectsConfiguration.class.getName(), configuration.get(), properties);
        }
    }

    private static class IsAoEntityPredicate implements Predicate<Class>
    {
        @Override
        public boolean apply(Class clazz)
        {
            return RawEntity.class.isAssignableFrom(clazz);
        }
    }
}