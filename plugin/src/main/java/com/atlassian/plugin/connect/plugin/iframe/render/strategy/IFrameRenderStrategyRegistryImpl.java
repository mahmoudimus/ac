package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.spi.event.ConnectAddonDisabledEvent;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Maintains a map of plugin + module key -> iframe rendering strategy.
 */
@Component
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

    public void register(final String addonKey, final String moduleKey, final IFrameRenderStrategy strategy)
    {
        synchronized (store) {
            Map<String, IFrameRenderStrategy> addonMap = store.get(addonKey);
            if (addonMap == null) {
                addonMap = Maps.newConcurrentMap();
                store.put(addonKey, addonMap);
            }
            addonMap.put(moduleKey, strategy);
        }
    }

    @Override
    public IFrameRenderStrategy get(final String addonKey, final String moduleKey)
    {
        Map<String, IFrameRenderStrategy> addonEndpoints = store.get(addonKey);
        return addonEndpoints == null ? null : addonEndpoints.get(moduleKey);
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
}
