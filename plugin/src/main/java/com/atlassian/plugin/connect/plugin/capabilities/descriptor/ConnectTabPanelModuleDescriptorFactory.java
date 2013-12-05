package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/*
 * NOTE: we are not implementing the "normal" factory interface because we need specialized method params
 */
@Component
public class ConnectTabPanelModuleDescriptorFactory
{
    private static final String KEY = "key";
    private static final String ORDER = "order";
    private static final String URL = "url";
    private static final String NAME = "name";
    private static final String LABEL = "label";
    private static final String CLASS = "class";
    private static final String CONDITION = "condition";
    
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConnectAutowireUtil connectAutowireUtil;

    @Autowired
    public ConnectTabPanelModuleDescriptorFactory(ConditionModuleFragmentFactory conditionModuleFragmentFactory, ConnectAutowireUtil connectAutowireUtil)
    {
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.connectAutowireUtil = connectAutowireUtil;
    }

    public ModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints)
    {
        DOMElement element = new DOMElement(hints.getDomElementName());
        
        String completeKey = hints.getModulePrefix() + bean.getKey();
        element
                .addAttribute(KEY,completeKey)
                .addAttribute(NAME,bean.getName().getValue())
                .addAttribute(URL,bean.getUrl());
        
        if(null != hints.getModuleClass())
        {
            element.addAttribute(CLASS, hints.getModuleClass().getName());
        }
        
        element.addElement(ORDER).setText(Integer.toString(bean.getWeight()));
        
        element.addElement(LABEL)
               .addAttribute(KEY, bean.getName().getI18n())
               .setText(bean.getName().getValue());

        if(!bean.getConditions().isEmpty())
        {
            element.add(conditionModuleFragmentFactory.createFragment(plugin.getKey(),bean.getConditions(),"#" + completeKey));
        }

        ModuleDescriptor descriptor = connectAutowireUtil.createBean(hints.getDescriptorClass());
        descriptor.init(plugin,element);
        
        return descriptor;
    }
}
