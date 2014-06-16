package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.google.common.base.Objects;
import org.dom4j.Element;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.util.EncodingUtils.escapeQuotes;
import static com.google.common.collect.Maps.newHashMap;

//TODO: make this NOT rely on xml elements!!!!
/**
 * Parameters for this iframe instance
 */
public class IFrameParamsImpl implements IFrameParams
{
    private final Map<String,Object> params = newHashMap();

    public IFrameParamsImpl()
    {
    }

    @XmlDescriptor
    public IFrameParamsImpl(Element module)
    {
        addToParams(module, "height");
        addToParams(module, "width");
    }

    @XmlDescriptor
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

    public void setParamNoEscape(String key, String value)
    {
        params.put(key, value);
    }

    @Override
    public Map<String, Object> getAsMap()
    {
        return params;
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("params", params).toString();
    }
}
