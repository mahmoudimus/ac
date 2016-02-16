package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.extension.connect.ConnectExtensionProvider;
import com.atlassian.extension.provider.api.ExtensionTag;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.descriptor.ConnectAddonBeanFactory;
import com.atlassian.vcache.RequestCache;
import com.atlassian.vcache.VCacheFactory;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.atlassian.plugin.connect.plugin.lifecycle.BeanToModuleRegistrar.getDescriptorsForBeans;

/**
 * Provides Extensions by returning module descriptors for enabled connect Addons.
 *
 * @since v1.1.74
 */
@Named
@ParametersAreNonnullByDefault
class ConnectExtensionManager implements ConnectExtensionProvider
{
    private static final Logger log = LoggerFactory.getLogger(ConnectExtensionManager.class);
    public static final String MODULE_DESCRIPTOR_CACHE_NAME = ConnectExtensionManager.class.getName() + ".moduleDescriptors";

    private final BundleContext bundleContext;
    private final EventPublisher eventPublisher;
    private final PluginAccessor pluginAccessor;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final ConnectAddonRegistry connectAddonRegistry;

    /**
     * Cache the map class -> module descriptors from Connect Addons which are instances of that class.
     *
     * The value at key SomeClass.class is in fact an instance of List<SomeClass>.
     */
    private final RequestCache<Class<?>, List<?>> moduleDescriptorCache;

    /**
     * Our OSGi registration as a ConnectExtensionProvider which the products use to wire us in.
     */
    private ServiceRegistration<?> serviceRegistration;

    @Inject
    ConnectExtensionManager(
            final BundleContext bundleContext,
            final EventPublisher eventPublisher,
            final PluginAccessor pluginAccessor,
            final VCacheFactory vcacheFactory,
            final ConnectAddonBeanFactory connectAddonBeanFactory,
            final ConnectAddonRegistry connectAddonRegistry)
    {
        this.bundleContext = bundleContext;
        this.eventPublisher = eventPublisher;
        this.pluginAccessor = pluginAccessor;
        this.connectAddonBeanFactory = connectAddonBeanFactory;
        this.connectAddonRegistry = connectAddonRegistry;
        this.moduleDescriptorCache = vcacheFactory.getRequestCache(MODULE_DESCRIPTOR_CACHE_NAME);
    }

    Map<Class, Integer> statistics = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public <E> ExtensionTag.Tagged<Iterable<E>> getExtensionsByClass(
            final Class<E> extensionClass, final Predicate<ExtensionTag> cached)
    {
        if (extensionClass.equals(ConnectModuleProviderModuleDescriptor.class))
        {
            // Connect Addons can't (currently) publish connect addon points themselves, so there can't be any
            // of these, and it breaks a recursion cycle because code in getDescriptorsForBeans. We can't just
            // pull our query out of there and push it up in the lifecycle, because other code which is more
            // decoupled also queries the available ConnectModuleProviderModuleDescriptor descriptors.
            // Longer term we'll probably filter a lot of connect impossible things here or be smart in other ways,
            // so this hack is ok for now.
            return new ExtensionTag.Tagged<>(ImmutableList.of());
        }
        else
        {
            // This logic is about populating the moduleDescriptorCache. We initially populate the Object.class entry,
            // since that is, by definition, every module descriptor from any Connect Addon, since everything is an
            // instance of Object. We can then populate any entry by filtering that list down.
            final List<?> extensionsUntyped = moduleDescriptorCache.get(extensionClass, () ->
            {
                // Populate the descriptors that subclass extensionClass by fetching all descriptors and then
                // filtering down to those that are instances of extensionClass
                final List<?> allModuleDescriptorsUntyped = moduleDescriptorCache.get(Object.class, () ->
                {
                    // Populate the descriptors that subclass Object, that is, everything, by parsing all json
                    // descriptors for all enabled addons and creating descriptors from the parsed json.

                    @SuppressWarnings ("UnnecessaryLocalVariable")
                    // Go via an explicit List<Object> so the compiler verifies the invariant for moduleDescriptorCache,
                    // namely that the entry this supplier returns for for Object.class is a List<Object>
                    final List<Object> objectList = connectAddonRegistry.getAllAddonKeys().stream()
                            .filter(key -> PluginState.ENABLED.equals(connectAddonRegistry.getRestartState(key)))
                            .map(key -> connectAddonBeanFactory.fromJson(connectAddonRegistry.getDescriptor(key)))
                            .flatMap(bean -> getDescriptorsForBeans(eventPublisher, pluginAccessor, bean).stream())
                            .collect(Collectors.toList());
                    return objectList;
                });
                @SuppressWarnings ("unchecked")
                // The cache entry for Class<Object> is a List<Object>, so this cast is safe.
                final List<Object> allModuleDescriptors = (List<Object>) allModuleDescriptorsUntyped;
                @SuppressWarnings ("UnnecessaryLocalVariable")
                // As above, go via explicit List<E> to verify the invariant for moduleDescriptorCache.
                final List<E> extensionList = (allModuleDescriptors).stream()
                        .filter(extensionClass::isInstance)
                        .map(extensionClass::cast)
                        .collect(Collectors.toList());
                return extensionList;
            });
            @SuppressWarnings ("unchecked")
            // The cache entry for Class<E> is a List<E>, so this cast is safe.
            final List<E> extensions = (List<E>) extensionsUntyped;
            return new ExtensionTag.Tagged<>(extensions);
        }
    }

    public void start()
    {
        if (null != serviceRegistration)
        {
            log.warn("Subsequent start() of ConnectExtensionManager ignored");
        }
        else
        {
            serviceRegistration = bundleContext.registerService(ConnectExtensionProvider.class.getName(), this, null);
        }
    }

    public void stop()
    {
        if (null == serviceRegistration)
        {
            log.warn("Request to stop() unstarted ConnectExtensionManager ignored");
        }
        else
        {
            serviceRegistration.unregister();
        }
    }
}
