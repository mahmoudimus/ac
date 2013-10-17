package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ParamsBean;

/**
 * @since version
 */
public class ParamsBeanBuilder
{
    private Map<String,String> params;
    
    public ParamsBeanBuilder()
    {
        this.params = new HashMap<String, String>();
    }
    
    public ParamsBeanBuilder withParams(Map<String,String> params)
    {
        this.params = params;
        return this;
    }
    
    public ParamsBean build()
    {
        return new ParamsBean(params);
    }
}
