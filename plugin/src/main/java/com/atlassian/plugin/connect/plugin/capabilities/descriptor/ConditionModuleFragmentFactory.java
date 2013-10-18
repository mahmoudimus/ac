package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.module.RemoteCondition;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Maps.newHashMap;

@Component
public class ConditionModuleFragmentFactory implements ConnectModuleFragmentFactory<List<ConditionalBean>>
{
    private final ProductAccessor productAccessor;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Autowired
    public ConditionModuleFragmentFactory(ProductAccessor productAccessor, RemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        this.productAccessor = productAccessor;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
    }

    @Override
    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans)
    {
        return createFragment(pluginKey, beans, null, Collections.<String>emptyList());
    }

    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans, String toHideSelector)
    {
        return createFragment(pluginKey, beans, toHideSelector, Collections.<String>emptyList());
    }

    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans, List<String> contextParams)
    {
        return createFragment(pluginKey, beans, null, contextParams);
    }

    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans, String toHideSelector, List<String> contextParams)
    {
        DOMElement element = new DOMElement("conditions");
        element.addAttribute("type", "AND");
        
        List<DOMElement> conditions = processConditionBeans(pluginKey, beans, toHideSelector, contextParams);
        
        for(DOMElement condition : conditions)
        {
            element.add(condition);
        }
        
        return element;

    }
    
    private List<DOMElement> processConditionBeans(String pluginKey, List<ConditionalBean> beans, String toHideSelector, List<String> contextParams)
    {
        List<DOMElement> elements = new ArrayList<DOMElement>();
        
        for(ConditionalBean bean : beans)
        {
            if(SingleConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                DOMElement element = createSingleCondition(pluginKey,(SingleConditionBean)bean,toHideSelector,contextParams);
                
                if(null != element)
                {
                    elements.add(element);
                }
            }
            else if(CompositeConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                DOMElement composite = new DOMElement("conditions");
                CompositeConditionBean ccb = (CompositeConditionBean) bean;
                
                composite.addAttribute("type",ccb.getType().toString().toUpperCase());
                
                List<DOMElement> subConditions = processConditionBeans(pluginKey, ccb.getConditions(), toHideSelector, contextParams);
                
                for(DOMElement subcondition : subConditions)
                {
                    composite.add(subcondition);
                }
                
                elements.add(composite);   
            }
        }
        
        return elements;
    }
 
    
    private DOMElement createSingleCondition(String pluginKey, SingleConditionBean bean, String toHideSelector, List<String> contextParams)
    {
        String className = "";
        DOMElement element = null;
        Map<String,String> params = newHashMap(bean.getParams());
        
        if(isRemoteCondition(bean))
        {
            String conditionUrl = bean.getCondition();
            if(conditionUrl.startsWith("/"))
            {
                RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(pluginKey);
                conditionUrl = remotablePluginAccessor.getBaseUrl().toString() + conditionUrl;
            }
            
            className = RemoteCondition.class.getName();
            
            params.put("pluginKey",pluginKey);
            params.put("url",conditionUrl);
            
            if(Strings.isNullOrEmpty(toHideSelector))
            {
                String hash = createUniqueUrlHash(pluginKey, conditionUrl);
                String hashedSelector = "." + hash;
                params.put("toHideSelector",hashedSelector);
            }
            
            if(null != contextParams && !contextParams.isEmpty())
            {
                params.put("contextParams", Joiner.on(",").join(contextParams));
            }
            
        }
        else
        {
            Class<? extends Condition> clazz = productAccessor.getConditions().get(bean.getCondition());
            
            if(null != clazz)
            {
                className = clazz.getName();
            }
        }
        
        if(!Strings.isNullOrEmpty(className))
        {
            element = new DOMElement("condition");
            element.addAttribute("class", className);
            element.addAttribute("invert",Boolean.toString(bean.isInvert()));
            
            for(Map.Entry<String,String> entry : params.entrySet())
            {
                element.addElement("param")
                       .addAttribute("name",entry.getKey())
                       .addAttribute("value",entry.getValue());
            }
        }
        
        return element;
    }
    
    private boolean isRemoteCondition(SingleConditionBean bean)
    {
        return (bean.getCondition().startsWith("http") || bean.getCondition().startsWith("/"));
    }

    private String createUniqueUrlHash(String pluginKey, String cUrl)
    {
        return "ap-hash-" + (cUrl + ":" + pluginKey).hashCode();
    }
}
