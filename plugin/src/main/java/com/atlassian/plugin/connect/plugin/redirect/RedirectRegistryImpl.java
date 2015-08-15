package com.atlassian.plugin.connect.plugin.redirect;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

@Component
public class RedirectRegistryImpl implements RedirectRegistry
{
    private final Map<String, Map<String, RedirectRegistry.RedirectData>> store = Maps.newConcurrentMap();

    @Override
    public void register(final String addonKey, final String moduleKey, final RedirectData redirectData)
    {
        synchronized (store) {
            Map<String, RedirectData> addonMap = store.get(addonKey);
            if (addonMap == null) {
                addonMap = Maps.newConcurrentMap();
                store.put(addonKey, addonMap);
            }
            addonMap.put(moduleKey, redirectData);
        }
    }

    @Override
    public RedirectData get(final String addonKey, final String moduleKey)
    {
        Map<String, RedirectData> addonEndpoints = store.get(addonKey);
        return addonEndpoints == null ? null : addonEndpoints.get(moduleKeyOnly(addonKey, moduleKey));
    }

}
