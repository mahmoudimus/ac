package com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.TabPanelDescriptorHints;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/*
 * NOTE: we are not implementing the "normal" factory interface because we need specialized method params
 */
@Component
@ExportAsDevService
public class DefaultConnectTabPanelModuleDescriptorFactory implements ConnectTabPanelModuleDescriptorFactory
{
    private static final String KEY = "key";
    private static final String ORDER = "order";
    private static final String URL = "url";
    private static final String NAME = "name";
    private static final String LABEL = "label";
    private static final String CLASS = "class";

    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;
    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public DefaultConnectTabPanelModuleDescriptorFactory(ConditionModuleFragmentFactory conditionModuleFragmentFactory, ConnectContainerUtil connectContainerUtil)
    {
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
        this.connectContainerUtil = connectContainerUtil;
    }

    @Override
    public ModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin theConnectPlugin, ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints)
    {
        DOMElement element = new DOMElement(hints.getDomElementName());

        element
                .addAttribute(KEY, bean.getKey(addon))
                .addAttribute(NAME, StringEscapeUtils.escapeHtml(bean.getName().getValue()))
                .addAttribute(URL, bean.getUrl());

        if (null != hints.getModuleClass())
        {
            element.addAttribute(CLASS, hints.getModuleClass().getName());
        }

        element.addElement(ORDER).setText(Integer.toString(bean.getWeight()));

        element.addElement(LABEL)
                .addAttribute(KEY, bean.getName().getI18n())
                                                 .setText(bean.getName().getValue());

        if (!bean.getConditions().isEmpty())
        {
            element.add(conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions()));
        }

        ModuleDescriptor descriptor = connectContainerUtil.createBean(hints.getDescriptorClass());
        descriptor.init(theConnectPlugin, element);

        return descriptor;
    }
}
