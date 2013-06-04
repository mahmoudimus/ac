package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

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
                        bundleContext.getService(
                                bundleContext.getServiceReference(PluginRetrievalService.class.getName()))
                ).getPlugin());

        bind(PluginEventIntegration.class).asEagerSingleton();

        registerPostConstructHandler();
        registerPreDestroyHandler();
        registerPublicComponentHandler();
    }

    private void registerPublicComponentHandler()
    {
        bindListener(Matchers.any(), new PublicComponentAnnotatedClassTypeListener(bundle.getBundleContext()));
    }

    private void registerPreDestroyHandler()
    {
        final Disposer disposer = new BundleDisposer(bundle);

        bindListener(Matchers.any(), new PreDestroyAbstractAnnotatedMethodTypeListener(disposer));
    }

    private void registerPostConstructHandler()
    {
        bindListener(Matchers.any(), new PostConstructAnnotatedMethodTypeListener());
    }
}
