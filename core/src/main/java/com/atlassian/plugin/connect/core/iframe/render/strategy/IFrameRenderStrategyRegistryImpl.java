package com.atlassian.plugin.connect.core.iframe.render.strategy;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 * Maintains a map of plugin + module key to iframe rendering strategy.
 */
@Component
@ExportAsDevService
public class IFrameRenderStrategyRegistryImpl implements IFrameRenderStrategyRegistry, InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;

    // all updates to the store or the contained maps should syncrhonize on 'store'
    private final Map<String, Map<String, IFrameRenderStrategy>> store = Maps.newConcurrentMap();

    @Autowired
    public IFrameRenderStrategyRegistryImpl(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void unregisterAll(final String addonKey)
    {
        synchronized (store) {
            store.remove(addonKey);
        }
    }

    @Override
    public void register(final String addonKey, final String moduleKey, final IFrameRenderStrategy strategy)
    {
        register(addonKey, moduleKey, null, strategy);
    }

    @Override
    public void register(final String addonKey, final String moduleKey, final String classifier, final IFrameRenderStrategy strategy)
    {
        synchronized (store) {
            Map<String, IFrameRenderStrategy> addonMap = store.get(addonKey);
            if (addonMap == null) {
                addonMap = Maps.newConcurrentMap();
                store.put(addonKey, addonMap);
            }
            addonMap.put(composeKey(moduleKey, classifier), strategy);
        }
    }

    @Override
    public IFrameRenderStrategy get(final String addonKey, final String moduleKey)
    {
        return get(addonKey, moduleKey, null);
    }

    @Override
    public IFrameRenderStrategy get(final String addonKey, final String moduleKey, final String classifier)
    {
        Map<String, IFrameRenderStrategy> addonEndpoints = store.get(addonKey);
        return addonEndpoints == null ? null : addonEndpoints.get(composeKey(moduleKeyOnly(addonKey, moduleKey), classifier));
    }

    @Override
    public IFrameRenderStrategy getOrThrow(final String addonKey, final String moduleKey) throws IllegalStateException
    {
        return getOrThrow(addonKey, moduleKey, null);
    }

    @Override
    public IFrameRenderStrategy getOrThrow(final String addonKey, final String moduleKey, final String classifier)
            throws IllegalStateException
    {
        IFrameRenderStrategy strategy = get(addonKey, moduleKey, classifier);
        if (strategy == null)
        {
            throw new IllegalStateException(String.format("No %s registered for %s:%s",
                    IFrameRenderStrategy.class.getSimpleName(), addonKey, composeKey(moduleKey, classifier)));
        }
        return strategy;
    }

    @EventListener
    public void onConnectPluginDisabled(ConnectAddonDisabledEvent event)
    {
        unregisterAll(event.getPluginKey());
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    private String composeKey(String moduleKey, String classifier)
    {
        return moduleKey + (Strings.isNullOrEmpty(classifier) ? "" : "|" + classifier);
    }
}
