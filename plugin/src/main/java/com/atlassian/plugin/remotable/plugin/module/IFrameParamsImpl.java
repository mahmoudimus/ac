package com.atlassian.plugin.remotable.plugin.module;

import com.atlassian.plugin.remotable.plugin.util.node.Node;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;

import java.util.Map;

import static com.atlassian.plugin.remotable.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Parameters for this iframe instance
 */
public final class IFrameParamsImpl implements IFrameParams
{
    private final Map<String,Object> params = newHashMap();
    
    public IFrameParamsImpl(Node module)
    {
        addToParams(module, "height");
        addToParams(module, "width");
    }
    public void addToParams(Node e, String key)
    {
        Node val = e.get(key);
        if (val.exists())
        {
            params.put(key, escapeQuotes(val.asString()));
        }
    }
    
    public void setParam(String key, String value)
    {
        params.put(key, escapeQuotes(value));
    }

    public void setParamNoEscape(String key, String value)
    {
        params.put(key, value);
    }

    @Override
    public Map<String, Object> getAsMap()
    {
        return params;
    }
}
