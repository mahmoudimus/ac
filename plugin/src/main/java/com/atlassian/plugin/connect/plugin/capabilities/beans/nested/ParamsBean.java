package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ParamsBeanBuilder;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ParamBean.newParamBean;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @since 1.0
 */
public class ParamsBean
{
    List<ParamBean> paramBeans;

    public ParamsBean()
    {
        this(new HashMap<String,String>());
    }

    public ParamsBean(Map<String,String> paramMap)
    {
        this.paramBeans = new ArrayList<ParamBean>();
        
        for(Map.Entry<String,String> entry : paramMap.entrySet())
        {
            paramBeans.add(newParamBean(entry.getKey(),entry.getValue()));
        }
    }
    
    public static ParamsBeanBuilder newParamsBean()
    {
        return new ParamsBeanBuilder();
    }

    public static ParamsBeanBuilder newParamsBean(Map<String,String> params)
    {
        return new ParamsBeanBuilder().withParams(params);
    }
    
    public Map<String,String> getParams()
    {
        Map<String,String> params = new HashMap<String, String>();
        
        for (ParamBean param : paramBeans)
        {
            params.put(param.getName(),param.getValue());
        }
        
        return params;
    }
}
