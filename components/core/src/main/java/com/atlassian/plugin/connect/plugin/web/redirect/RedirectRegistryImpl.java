package com.atlassian.plugin.connect.plugin.web.redirect;

import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

@Component
public class RedirectRegistryImpl implements RedirectRegistry
{
    private final Map<String, Map<String, RedirectData>> store = Maps.newConcurrentMap();

    @Override
    public void register(String addonKey, String moduleKey, RedirectData redirectData)
    {
        synchronized (store)
        {

            Map<String, RedirectData> addonMap = store.get(addonKey);
            if (addonMap == null)
            {
                addonMap = Maps.newConcurrentMap();
                store.put(addonKey, addonMap);
            }
            addonMap.put(moduleKey, redirectData);
        }
    }

    @Override
    public Optional<RedirectData> get(String addonKey, String moduleKey)
    {
        Map<String, RedirectData> addonEndpoints = store.get(addonKey);
        if (addonEndpoints == null)
        {
            return Optional.empty();
        }
        return Optional.of(addonEndpoints.get(moduleKeyOnly(addonKey, moduleKey)));
    }
}
