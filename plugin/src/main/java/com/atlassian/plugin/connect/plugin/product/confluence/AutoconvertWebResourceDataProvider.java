package com.atlassian.plugin.connect.plugin.product.confluence;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.webresource.api.data.WebResourceDataProvider;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class AutoconvertWebResourceDataProvider implements WebResourceDataProvider
{
    private final PluginAccessor pluginAccessor;

    public AutoconvertWebResourceDataProvider(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Jsonable get()
    {
        return new Jsonable()
        {
            @Override
            public void write(Writer writer) throws IOException
            {
                List<Map<String,Object>> resultList = new ArrayList<Map<String, Object>>();
                List<AutoconvertModuleDescriptor> list = pluginAccessor.getEnabledModuleDescriptorsByClass(AutoconvertModuleDescriptor.class);

                for (AutoconvertModuleDescriptor descriptor : list)
                {
                    Map<String,Object> item = new HashMap<String,Object>();
                    item.put("macroName",descriptor.getMacroName());
                    item.put("autoconvert",descriptor.getModule());
                    resultList.add(item);
                }
                Gson gson = ConnectModulesGsonFactory.getGson();
                writer.write(gson.toJson(resultList));
            }
        };
    }
}
