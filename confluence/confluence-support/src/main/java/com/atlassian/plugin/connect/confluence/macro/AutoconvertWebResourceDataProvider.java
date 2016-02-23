package com.atlassian.plugin.connect.confluence.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.webresource.api.data.WebResourceDataProvider;

import com.google.gson.Gson;

/**
 * A {@link com.atlassian.webresource.api.data.WebResourceDataProvider} that defines all autoconvert definitions.
 *
 * This data provider is used by the autoconvert javascript to link the autoconvert configuration in all connect add-ons
 * and the autoconvert logic in the tiny mce editor.
 */
public class AutoconvertWebResourceDataProvider implements WebResourceDataProvider {
    private final PluginAccessor pluginAccessor;

    public AutoconvertWebResourceDataProvider(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Jsonable get() {
        return writer -> {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List<AutoconvertModuleDescriptor> list = pluginAccessor.getEnabledModuleDescriptorsByClass(AutoconvertModuleDescriptor.class);

            for (AutoconvertModuleDescriptor descriptor : list) {
                Map<String, Object> item = new HashMap<>();
                item.put("macroName", descriptor.getMacroName());
                item.put("autoconvert", descriptor.getModule());
                item.put("matcherBean", descriptor.getMatcherBean());
                resultList.add(item);
            }
            Gson gson = ConnectModulesGsonFactory.getGson();
            writer.write(gson.toJson(resultList));
        };
    }
}
