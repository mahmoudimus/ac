package com.atlassian.plugin.remotable.plugin.module;

import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import org.dom4j.Element;

import java.util.Map;

import static com.atlassian.plugin.remotable.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Parameters for this iframe instance
 */
public final class IFrameParamsImpl implements IFrameParams
{
    private final Map<String,Object> params = newHashMap();
    
    public IFrameParamsImpl(Element module)
    {
        addToParams(module, "height");
        addToParams(module, "width");
    }
    public void addToParams(Element e, String key)
    {
        String val = e.attributeValue(key);
        if (val != null)
        {
            params.put(key, escapeQuotes(val));
        }
    }
    
    public void setParam(String key, String value)
    {
        params.put(key, escapeQuotes(value));
    }

    @Override
    public Map<String, Object> getAsMap()
    {
        return params;
    }
}
