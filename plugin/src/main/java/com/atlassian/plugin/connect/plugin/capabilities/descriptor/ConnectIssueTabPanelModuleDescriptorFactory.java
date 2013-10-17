package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * A factory to produce a ConnectIssueTabPanelModuleDescriptor from a ConnectIssueTabPanelCapabilityBean
 */
// Turning off component scanning until ACDEV-445 is resolved
//@Component
public class ConnectIssueTabPanelModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ConnectIssueTabPanelCapabilityBean, ConnectIssueTabPanelModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConnectIssueTabPanelModuleDescriptorFactory.class);


    private final IconModuleFragmentFactory iconModuleFragmentFactory;
    private final ConnectAutowireUtil connectAutowireUtil;

    //    @Autowired
    public ConnectIssueTabPanelModuleDescriptorFactory(IconModuleFragmentFactory iconModuleFragmentFactory, ConnectAutowireUtil connectAutowireUtil)
    {
        this.connectAutowireUtil = connectAutowireUtil;
        this.iconModuleFragmentFactory = checkNotNull(iconModuleFragmentFactory);
    }


    @Override
    public ConnectIssueTabPanelModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, ConnectIssueTabPanelCapabilityBean bean)
    {
        Element issueTabPageElement = new DOMElement("issue-tab-page");

        String issueTabPageKey = bean.getKey();

        issueTabPageElement.addAttribute("key", issueTabPageKey);
        issueTabPageElement.addElement("order").setText(Integer.toString(bean.getWeight()));
        issueTabPageElement.addAttribute("url", bean.getUrl());
        issueTabPageElement.addAttribute("name", bean.getName().getValue());

        issueTabPageElement.addElement("label")
                .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                .setText(escapeHtml(bean.getName().getValue()));

        issueTabPageElement.addElement("condition").addAttribute("class", DynamicMarkerCondition.class.getName());

        //TODO: implement condition beans and grab the condition from the bean. e.g. bean.getConditioon();
//        if (conditionClass != null)
//        {
//            issueTabPageElement.addElement("condition").addAttribute("class", conditionClass.getName());
//        }
//
//        Condition condition = conditionProcessor.process(configurationElement, issueTabPageElement, plugin.getKey());
//        
//        if (condition instanceof ContainingRemoteCondition)
//        {
//            styleClasses.add("remote-condition");
//            styleClasses.add("hidden");
//            styleClasses.add(conditionProcessor.createUniqueUrlHash(plugin.getKey(), ((ContainingRemoteCondition) condition).getConditionUrl()));
//        }

        if (log.isDebugEnabled())
        {
            log.debug("Created issue tab page: " + printNode(issueTabPageElement));
        }

        issueTabPageElement.addAttribute("system", "true");

        ConnectIssueTabPanelModuleDescriptor descriptor = connectAutowireUtil.createBean(ConnectIssueTabPanelModuleDescriptor.class);

        descriptor.init(plugin, issueTabPageElement);

        return descriptor;
    }


}
