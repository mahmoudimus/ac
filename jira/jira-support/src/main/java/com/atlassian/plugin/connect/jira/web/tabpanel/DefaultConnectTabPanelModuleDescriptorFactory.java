package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;


/*
 * NOTE: we are not implementing the "normal" factory interface because we need specialized method params
 */
@JiraComponent
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
    public ModuleDescriptor createModuleDescriptor(ConnectAddonBean connectAddonBean, Plugin plugin, ConnectTabPanelModuleBean bean, TabPanelDescriptorHints hints)
    {
        DOMElement element = new DOMElement(hints.getDomElementName());

        element
                .addAttribute(KEY, bean.getKey(connectAddonBean))
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
            element.add(conditionModuleFragmentFactory.createFragment(connectAddonBean.getKey(), bean.getConditions()));
        }

        ModuleDescriptor descriptor = connectContainerUtil.createBean(hints.getDescriptorClass());
        descriptor.init(plugin, element);

        return descriptor;
    }
}
