package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;

import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Component
public class ConnectTabPanelModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConnectTabPanelCapabilityBean, ModuleDescriptor>
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

    @Override
    public ModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, ConnectTabPanelCapabilityBean bean)
    {
        TabPanelDescriptorHints hints = bean.getDescriptorHints();
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
               .addAttribute(KEY, escapeHtml(bean.getName().getI18n()))
               .setText(escapeHtml(bean.getName().getValue()));

        if(!bean.getConditions().isEmpty())
        {
            DOMElement conditions = conditionModuleFragmentFactory.createFragment(plugin.getKey(),bean.getConditions(),"#" + completeKey);

            if(null != conditions)
            {
                element.add(conditions);
            }
        }

        ModuleDescriptor descriptor = connectAutowireUtil.createBean(hints.getDescriptorClass());
        descriptor.init(plugin,element);
        
        return descriptor;
    }
}
