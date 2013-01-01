package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import org.ops4j.peaberry.Peaberry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ops4j.peaberry.Peaberry.osgiModule;

public final class RemotableModule extends AbstractModule
{
    private final Bundle bundle;

    public RemotableModule(Bundle bundle)
    {
        this.bundle = checkNotNull(bundle);
    }

    @Override
    protected void configure()
    {
        final BundleContext bundleContext = bundle.getBundleContext();

        install(osgiModule(bundleContext));

        bind(ModuleFactory.class).toInstance(
                (ModuleFactory)
                        bundleContext.getService(bundleContext.getServiceReference(ModuleFactory.class.getName()))
        );

        bind(Plugin.class).toInstance(
                ((PluginRetrievalService)
                        bundleContext.getService(bundleContext.getServiceReference(PluginRetrievalService.class.getName()))
                ).getPlugin());

        bind(PluginEventIntegration.class).asEagerSingleton();

        registerPostConstructHandler();
        registerPreDestroyHandler();
    }

    private void registerPreDestroyHandler()
    {
        final Disposer disposer = new BundleDisposer(bundle);

        bindListener(Matchers.any(), new AbstractMethodTypeListener(PreDestroy.class)
        {
            @Override
            protected <I> void hear(final Method method, TypeEncounter<I> encounter)
            {
                encounter.register(new InjectionListener<I>()
                {
                    public void afterInjection(I injectee)
                    {
                        disposer.register(method, injectee);
                    }
                });
            }
        });
    }

    private void registerPostConstructHandler()
        {
            bindListener(Matchers.any(), new AbstractMethodTypeListener(PostConstruct.class)
            {
                @Override
                protected <I> void hear(final Method method, TypeEncounter<I> encounter)
                {
                    encounter.register(new InjectionListener<I>()
                    {
                        public void afterInjection(I injectee)
                        {
                            try
                            {
                                method.invoke(injectee);
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new RuntimeException(e);
                            }
                            catch (InvocationTargetException e)
                            {
                                throw new RuntimeException(e);
                            }
                            catch (Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            });
        }
}
