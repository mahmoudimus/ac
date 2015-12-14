package com.atlassian.plugin.connect.plugin.web.redirect;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.lifecycle.event.ConnectAddonDisabledEvent;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

@Component
public class RedirectRegistryImpl implements RedirectRegistry, InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;

    private final Map<String, Map<String, RedirectData>> store = Maps.newConcurrentMap();

    @Autowired
    public RedirectRegistryImpl(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void register(String addOnKey, String moduleKey, RedirectData redirectData)
    {
        String moduleKeyOnly = ModuleKeyUtils.moduleKeyOnly(addOnKey, moduleKey);
        store.compute(addOnKey, new BiFunction<String, Map<String, RedirectData>, Map<String, RedirectData>>()
        {
            @Override
            public Map<String, RedirectData> apply(String key, Map<String, RedirectData> addonMap)
            {
                if (addonMap == null)
                {
                    addonMap = Maps.newConcurrentMap();
                }
                addonMap.put(moduleKeyOnly, redirectData);
                return addonMap;
            }
        });
    }

    @Override
    public void unregisterAll(final String addonKey)
    {
        store.remove(addonKey);
    }

    @Override
    public Optional<RedirectData> get(String addOnKey, String moduleKey)
    {
        String moduleKeyOnly = ModuleKeyUtils.moduleKeyOnly(addOnKey, moduleKey);
        Map<String, RedirectData> addonEndpoints = store.get(addOnKey);
        if (addonEndpoints == null || !addonEndpoints.containsKey(moduleKeyOnly))
        {
            return Optional.empty();
        }
        return Optional.of(addonEndpoints.get(moduleKeyOnly(addOnKey, moduleKeyOnly)));
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
