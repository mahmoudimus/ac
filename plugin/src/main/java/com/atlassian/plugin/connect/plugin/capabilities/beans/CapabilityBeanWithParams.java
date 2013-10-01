package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.util.List;
import java.util.Map;

/**
 * @since version
 */
public interface CapabilityBeanWithParams
{
    List<CapabilityParamBean> getParamBeans();
    
    Map<String,String> getParamsAsMap();
}
