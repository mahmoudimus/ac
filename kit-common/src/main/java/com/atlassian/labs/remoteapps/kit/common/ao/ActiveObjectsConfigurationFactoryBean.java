package com.atlassian.labs.remoteapps.kit.common.ao;

import com.atlassian.activeobjects.config.ActiveObjectsConfiguration;
import com.atlassian.activeobjects.config.ActiveObjectsConfigurationFactory;
import com.atlassian.activeobjects.external.ActiveObjectsUpgradeTask;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import net.java.ao.RawEntity;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Suppliers.*;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;

public final class ActiveObjectsConfigurationFactoryBean implements FactoryBean, DisposableBean
{
    private final Supplier<ServiceRegistration> registration;
    private final Supplier<ActiveObjectsConfiguration> configuration;

    public ActiveObjectsConfigurationFactoryBean(ApplicationContext applicationContext, final BundleContext bundleContext, final ActiveObjectsConfigurationFactory factory)
    {
        this.configuration = memoize(synchronizedSupplier(new ActiveObjectsConfigurationSupplier(applicationContext, bundleContext, factory)));
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
        private final ApplicationContext applicationContext;
        private final BundleContext bundleContext;
        private final ActiveObjectsConfigurationFactory factory;

        public ActiveObjectsConfigurationSupplier(ApplicationContext applicationContext, BundleContext bundleContext, ActiveObjectsConfigurationFactory factory)
        {
            this.applicationContext = checkNotNull(applicationContext);
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
            // not typing the iterable here, because of the cast afterward, which wouldn't compile otherwise!
            final Iterable upgradeClasses =
                    new BundleContextScanner().findClasses(
                            bundleContext,
                            "ao.upgrade",
                            new LoadClassFromBundleFunction(bundleContext.getBundle()),
                            new IsAoUpgradeTaskPredicate()
                    );

            @SuppressWarnings("unchecked") // we're filtering to get what we want!
            final Iterable<Class<? extends ActiveObjectsUpgradeTask>> upgrades = (Iterable<Class<? extends ActiveObjectsUpgradeTask>>) upgradeClasses;

            return copyOf(transform(upgrades, new Function<Class<? extends ActiveObjectsUpgradeTask>, ActiveObjectsUpgradeTask>()
            {
                @Override
                public ActiveObjectsUpgradeTask apply(Class<? extends ActiveObjectsUpgradeTask> input)
                {
                    return (ActiveObjectsUpgradeTask) applicationContext.getAutowireCapableBeanFactory()
                            .createBean(input, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
                }
            }));
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

    private static class IsAoUpgradeTaskPredicate implements Predicate<Class>
    {
        @Override
        public boolean apply(Class clazz)
        {
            return ActiveObjectsUpgradeTask.class.isAssignableFrom(clazz);
        }
    }
}