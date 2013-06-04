package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.remotable.plugin.loader.StartableForPlugins;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Refreshes any plugins that are wired to old api versions
 */
@Component
public class DependencyRefresher
{
    private static final Logger log = LoggerFactory.getLogger(DependencyRefresher.class);

    @Autowired
    public DependencyRefresher(final StartableForPlugins startableForPlugins,
            PluginRetrievalService pluginRetrievalService, final BundleContext context)
    {
        startableForPlugins.register(pluginRetrievalService.getPlugin().getKey(), new Runnable()
        {
            @Override
            public void run()
            {
                final ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
                final PackageAdmin packageAdmin = (PackageAdmin) context.getService(ref);

                final ServiceReference ref2 = context.getServiceReference(PluginController.class.getName());
                final PluginController pluginController = (PluginController) context.getService(ref2);

                final Set<Bundle> bundlesUsingOldApi = getRequiredPluginsFromExports(context, packageAdmin);
                if (!bundlesUsingOldApi.isEmpty())
                {
                    log.info("Detected bundles using old api versions, refreshing " +
                            (transform(bundlesUsingOldApi, new Function<Bundle, String>()
                            {
                                @Override
                                public String apply(@Nullable Bundle input)
                                {
                                    return input.getBundleId() + ": " + input.getSymbolicName() + " - " + input.getVersion();
                                }
                            })).toString());


                    final Iterable<String> pluginKeys = transform(filter(bundlesUsingOldApi, new Predicate<Bundle>()
                    {
                        @Override
                        public boolean apply(@Nullable Bundle input)
                        {
                            return OsgiHeaderUtil.getPluginKey(input) != null;
                        }
                    }), new Function<Bundle, String>()
                    {
                        @Override
                        public String apply(@Nullable Bundle input)
                        {
                            return OsgiHeaderUtil.getPluginKey(input);
                        }
                    });

                    for (String pluginKey : pluginKeys)
                    {
                        pluginController.disablePluginWithoutPersisting(pluginKey);
                    }
                    packageAdmin.refreshPackages(bundlesUsingOldApi.toArray(new Bundle[bundlesUsingOldApi.size()]));
                    pluginController.enablePlugins(newArrayList(pluginKeys).toArray(new String[0]));
                }
            }
        });

    }

    private Set<Bundle> getRequiredPluginsFromExports(BundleContext bundleContext, PackageAdmin packageAdmin)
    {
        Set<Bundle> bundlesUsingOldApi = newHashSet();
        Version latestApiVersion = packageAdmin.getExportedPackage(ComponentImport.class.getPackage().getName()).getVersion();

        // Get a set of all packages that this plugin imports
        final Set<String> imports = getApiAndSpiImports(bundleContext, packageAdmin);

        // For each import, determine what bundle provides the package
        for (final String imp : imports)
        {
            // Get a list of package exports for this package
            final ExportedPackage[] exports = packageAdmin.getExportedPackages(imp);
            if (exports != null)
            {
                // For each exported package, determine if we are a consumer
                for (final ExportedPackage export : exports)
                {
                    // Only bother with the export if it is an old one
                    if (!export.getVersion().equals(latestApiVersion))
                    {
                        // Get a list of bundles that consume that package
                        final Bundle[] importingBundles = export.getImportingBundles();
                        if (importingBundles != null)
                        {
                            // For each importing bundle, determine if it is us
                            for (final Bundle importingBundle : importingBundles)
                            {
                                // If the bundle isn't us and doesn't import the same version
                                if (!bundleContext.getBundle().getSymbolicName().equals(importingBundle.getSymbolicName()) &&
                                        !importingBundle.getSymbolicName().startsWith("com.atlassian.plugins.remotable-plugins-") &&
                                        !importingBundle.getSymbolicName().equals(I18nPropertiesPluginManager.I18N_SYMBOLIC_NAME))
                                {
                                    bundlesUsingOldApi.add(importingBundle);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return bundlesUsingOldApi;
    }

    private Set<String> getApiAndSpiImports(BundleContext bundleContext, PackageAdmin packageAdmin)
    {
        Set<String> imports = new HashSet<String>();
        for (Bundle bundle : bundleContext.getBundles())
        {
            if (bundle.getSymbolicName().equals("com.atlassian.plugins.remotable-plugins-api") ||
                    bundle.getSymbolicName().equals("com.atlassian.plugins.remotable-plugins-spi"))
            {
                imports.addAll(
                        OsgiHeaderUtil.parseHeader((String) bundle.getHeaders().get(Constants.EXPORT_PACKAGE)).keySet());
            }
        }
        return imports;
    }
}
