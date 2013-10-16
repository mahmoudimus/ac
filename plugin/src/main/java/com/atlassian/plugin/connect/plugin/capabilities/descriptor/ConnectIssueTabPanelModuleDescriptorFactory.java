package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

// Turning off component scanning until ACDEV-445 is resolved
//@Component
public class ConnectIssueTabPanelModuleDescriptorFactory implements ConnectModuleDescriptorFactory<IssueTabPageCapabilityBean, ConnectIssueTabPanelModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ConnectIssueTabPanelModuleDescriptorFactory.class);


    private final IconModuleFragmentFactory iconModuleFragmentFactory;

//    @Autowired
    public ConnectIssueTabPanelModuleDescriptorFactory(IconModuleFragmentFactory iconModuleFragmentFactory)
    {
        this.iconModuleFragmentFactory = checkNotNull(iconModuleFragmentFactory);
    }


    @Override
    public ConnectIssueTabPanelModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, IssueTabPageCapabilityBean bean)
    {
        Element issueTabPageElement = new DOMElement("issue-tab-page");

        String issueTabPageKey = bean.getKey();

        issueTabPageElement.addAttribute("key", issueTabPageKey);
        issueTabPageElement.addElement("order").setText(Integer.toString(bean.getWeight()));
        issueTabPageElement.addAttribute("url", bean.getUrl());
        issueTabPageElement.addAttribute("name", bean.getName().getValue());
//        issueTabPageElement.addAttribute("label", bean.getName().getValue());

        issueTabPageElement.addElement("label")
                .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                .setText(escapeHtml(bean.getName().getValue()));

//        Element linkElement = issueTabPageElement.addElement("url").addAttribute("linkId", issueTabPageKey);
//        linkElement.setText(bean.getUrl());

        if (null != bean.getIcon() && !Strings.isNullOrEmpty(bean.getIcon().getUrl()))
        {
            issueTabPageElement.add(iconModuleFragmentFactory.createFragment(plugin.getKey(), bean.getIcon()));
        }

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

        ConnectIssueTabPanelModuleDescriptor descriptor = ((ContainerManagedPlugin)plugin).getContainerAccessor().createBean(ConnectIssueTabPanelModuleDescriptor.class);

        descriptor.init(plugin, issueTabPageElement);

        return descriptor;
    }


}
