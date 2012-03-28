package com.atlassian.labs.remoteapps.modules;

import org.dom4j.Element;

import java.util.Map;

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
            params.put(key, val);
        }
    }
    
    public void setParam(String key, String value)
    {
        params.put(key, value);
    }

    public Map<String, Object> getAsMap()
    {
        return params;
    }
}
