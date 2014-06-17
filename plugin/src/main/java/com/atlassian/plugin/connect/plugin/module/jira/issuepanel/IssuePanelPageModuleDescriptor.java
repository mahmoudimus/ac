package com.atlassian.plugin.connect.plugin.module.jira.issuepanel;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.webpanel.RemoteWebPanelModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

/**
 * A view issue panel page that loads is contents from an iframe.
 * @deprecated use {@link com.atlassian.plugin.connect.plugin.module.webpanel.RemoteWebPanelModuleDescriptor} with
 * "atl.jira.view.issue.right.context" location instead.
 */
@Deprecated
@XmlDescriptor
public final class IssuePanelPageModuleDescriptor extends RemoteWebPanelModuleDescriptor
{
    public IssuePanelPageModuleDescriptor(
            ModuleFactory moduleFactory,
            IFrameRendererImpl iFrameRenderer,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            HostContainer hostContainer,
            BundleContext bundleContext,
            ConditionProcessor conditionProcessor,
            ContextMapURLSerializer contextMapURLSerializer,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor,
            UrlValidator urlValidator)
    {
        super(moduleFactory, iFrameRenderer, dynamicDescriptorRegistration, hostContainer, bundleContext, conditionProcessor, contextMapURLSerializer, userManager, urlVariableSubstitutor, urlValidator);
    }

    @Override
    protected String getLocation(final Element element)
    {
        return "atl.jira.view.issue.right.context";
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
