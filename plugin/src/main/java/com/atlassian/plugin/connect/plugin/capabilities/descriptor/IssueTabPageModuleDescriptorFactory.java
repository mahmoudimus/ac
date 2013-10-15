package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.issuetab.IssueTabPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class IssueTabPageModuleDescriptorFactory implements ConnectModuleDescriptorFactory<IssueTabPageCapabilityBean, IssueTabPageModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(IssueTabPageModuleDescriptorFactory.class);


    private final IconModuleFragmentFactory iconModuleFragmentFactory;
    private final ModuleFactory moduleFactory;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final ConditionProcessor conditionProcessor;

    private final IFrameRenderer iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UrlValidator urlValidator;
    private final ProjectSerializer projectSerializer;
    private final IssueSerializer issueSerializer;


    @Autowired
    public IssueTabPageModuleDescriptorFactory(IconModuleFragmentFactory iconModuleFragmentFactory,
                                               ModuleFactory moduleFactory, DynamicDescriptorRegistration dynamicDescriptorRegistration,
                                               ConditionProcessor conditionProcessor,
                                               IFrameRenderer iFrameRenderer, UrlVariableSubstitutor urlVariableSubstitutor,
                                               JiraAuthenticationContext jiraAuthenticationContext, UrlValidator urlValidator,
                                               ProjectSerializer projectSerializer, IssueSerializer issueSerializer)
    {
        this.projectSerializer = checkNotNull(projectSerializer);
        this.issueSerializer = checkNotNull(issueSerializer);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.iconModuleFragmentFactory = checkNotNull(iconModuleFragmentFactory);
        this.moduleFactory = checkNotNull(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.conditionProcessor = checkNotNull(conditionProcessor);
        this.urlValidator = checkNotNull(urlValidator);
    }


    @Override
    public IssueTabPageModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, IssueTabPageCapabilityBean bean)
    {
        Element issueTabPageElement = new DOMElement("issue-tab-page");

        String issueTabPageKey = bean.getKey();

        issueTabPageElement.addAttribute("key", issueTabPageKey);
        issueTabPageElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        issueTabPageElement.addAttribute("url", bean.getUrl());
        issueTabPageElement.addAttribute("name", bean.getName().getValue());

//        issueTabPageElement.addElement("label")
//                .addAttribute("key", escapeHtml(bean.getName().getI18n()))
//                .setText(escapeHtml(bean.getName().getValue()));

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

        return createIssueTabPageDescriptor(plugin, issueTabPageElement, issueTabPageKey, bean.getUrl());
    }

    private IssueTabPageModuleDescriptor createIssueTabPageDescriptor(Plugin plugin, Element issueTabPageElement, String key, String url)
    {
        issueTabPageElement.addAttribute("system", "true");

        IssueTabPageModuleDescriptor descriptor = new IssueTabPageModuleDescriptor(moduleFactory, dynamicDescriptorRegistration,
                conditionProcessor, iFrameRenderer, urlVariableSubstitutor, jiraAuthenticationContext, urlValidator,
                projectSerializer, issueSerializer);

        descriptor.init(plugin, issueTabPageElement);

        return descriptor;
    }

}
