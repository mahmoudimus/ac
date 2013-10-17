package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class AbstractConnectTabPanelModuleDescriptorFactory<B extends AbstractConnectTabPanelCapabilityBean, D extends ModuleDescriptor>
        implements ConnectModuleDescriptorFactory<B, D>
{
    private static final Logger log = LoggerFactory.getLogger(ConnectIssueTabPanelModuleDescriptorFactory.class);
    private final Class<D> descriptorClass;
    private final ConnectAutowireUtil connectAutowireUtil;
    private String domElementName;

    public AbstractConnectTabPanelModuleDescriptorFactory(Class<D> descriptorClass, String domElementName, ConnectAutowireUtil connectAutowireUtil)
    {
        this.domElementName = domElementName;

        this.descriptorClass = descriptorClass;
        this.connectAutowireUtil = connectAutowireUtil;
    }

    @Override
    public D createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, B bean)
    {
        Element domElement = new DOMElement(domElementName);

        String issueTabPageKey = bean.getKey();

        domElement.addAttribute("key", issueTabPageKey);
        domElement.addElement("order").setText(Integer.toString(bean.getWeight()));
        domElement.addAttribute("url", bean.getUrl());
        domElement.addAttribute("name", bean.getName().getValue());

        domElement.addElement("label")
                .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                .setText(escapeHtml(bean.getName().getValue()));

        domElement.addElement("condition").addAttribute("class", DynamicMarkerCondition.class.getName());

        //TODO: implement condition beans and grab the condition from the bean. e.g. bean.getConditioon();
//        if (conditionClass != null)
//        {
//            domElement.addElement("condition").addAttribute("class", conditionClass.getName());
//        }
//
//        Condition condition = conditionProcessor.process(configurationElement, domElement, plugin.getKey());
//
//        if (condition instanceof ContainingRemoteCondition)
//        {
//            styleClasses.add("remote-condition");
//            styleClasses.add("hidden");
//            styleClasses.add(conditionProcessor.createUniqueUrlHash(plugin.getKey(), ((ContainingRemoteCondition) condition).getConditionUrl()));
//        }

        if (log.isDebugEnabled())
        {
            log.debug("Created tab page: " + printNode(domElement));
        }

        domElement.addAttribute("system", "true");

        D descriptor = connectAutowireUtil.createBean(descriptorClass);

        descriptor.init(plugin, domElement);

        return descriptor;
    }
}
