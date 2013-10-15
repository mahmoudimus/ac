package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.issuetab.IssueTabPageModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Component
public class IssueTabPageModuleDescriptorFactory implements ConnectModuleDescriptorFactory<IssueTabPageCapabilityBean,IssueTabPageModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(IssueTabPageModuleDescriptorFactory.class);

    //TODO: rename this class to RemoteIssueTabPageModuleDescriptorFactory
    private final IssueTabPageModuleDescriptor remoteIssueTabPageDescriptorFactory;

    private final IconModuleFragmentFactory iconModuleFragmentFactory;

    @Autowired
    public IssueTabPageModuleDescriptorFactory(IssueTabPageModuleDescriptor remoteIssueTabPageDescriptorFactory, IconModuleFragmentFactory iconModuleFragmentFactory)
    {
        this.remoteIssueTabPageDescriptorFactory = remoteIssueTabPageDescriptorFactory;
        this.iconModuleFragmentFactory = iconModuleFragmentFactory;
    }

    @Override
    public IssueTabPageModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, IssueTabPageCapabilityBean bean)
    {
        Element issueTabPageElement = new DOMElement("issue-tab-page");

        String issueTabPageKey = bean.getKey();
        
        issueTabPageElement.addAttribute("key", issueTabPageKey);
        issueTabPageElement.addAttribute("weight", Integer.toString(bean.getWeight()));

        issueTabPageElement.addElement("label")
                      .addAttribute("key", escapeHtml(bean.getName().getI18n()))
                      .setText(escapeHtml(bean.getName().getValue()));

        Element linkElement = issueTabPageElement.addElement("link").addAttribute("linkId", issueTabPageKey);
        linkElement.setText(bean.getUrl());

        if(null != bean.getIcon() && !Strings.isNullOrEmpty(bean.getIcon().getUrl()))
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

        return createIssueTabPageDescriptor(plugin, issueTabPageElement, issueTabPageKey, bean.getUrl());
    }

    private IssueTabPageModuleDescriptor createIssueTabPageDescriptor(Plugin plugin, Element issueTabPageElement, String key, String url)
    {
        issueTabPageElement.addAttribute("system", "true");

        // TODO: where does url go?
        IFrameParamsImpl iFrameParams = new IFrameParamsImpl(issueTabPageElement);
//        iFrameParams.

        Condition condition = null; // TODO:

         // seems we may have to write a factory for IssueTabPageModuleDescriptor
//        new IssueTabPageModuleDescriptor();
//        final JiraResourcedModuleDescriptor descriptor = remoteIssueTabPageDescriptorFactory.createTabPanelModuleDescriptor(key, iFrameParams, condition);
//
//        descriptor.init(plugin, issueTabPageElement);
//
//        return descriptor;
        return null;
    }

}
