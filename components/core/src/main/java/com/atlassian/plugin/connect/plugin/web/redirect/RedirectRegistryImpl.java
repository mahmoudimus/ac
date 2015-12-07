package com.atlassian.plugin.connect.plugin.web.redirect;

import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
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
    public void register(String addOnKey, String moduleKey, RedirectData redirectData)
    {
        String moduleKeyOnly = ModuleKeyUtils.moduleKeyOnly(addOnKey, moduleKey);
        synchronized (store)
        {
            Map<String, RedirectData> addonMap = store.get(addOnKey);
            if (addonMap == null)
            {
                addonMap = Maps.newConcurrentMap();
                store.put(addOnKey, addonMap);
            }
            addonMap.put(moduleKeyOnly, redirectData);
        }
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
}
