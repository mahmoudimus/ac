package com.atlassian.labs.remoteapps.plugin.module;

import org.dom4j.Element;

import java.util.Map;

import static com.atlassian.labs.remoteapps.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Parameters for this iframe instance
 */
public class IFrameParams
{
    private final Map<String,Object> params = newHashMap();
    
    public IFrameParams(Element module)
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

    public Map<String, Object> getAsMap()
    {
        return params;
    }
}
