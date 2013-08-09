package com.atlassian.plugin.connect.plugin.module.jira.issuepanel;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.module.webpanel.RemoteWebPanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.webpanel.extractor.WebPanelURLParametersSerializer;
import com.atlassian.sal.api.user.UserManager;

import org.dom4j.Element;
import org.osgi.framework.BundleContext;

/**
 * A view issue panel page that loads is contents from an iframe.
 * @deprecated use {@link com.atlassian.plugin.connect.plugin.module.webpanel.RemoteWebPanelModuleDescriptor} with
 * "atl.jira.view.issue.right.context" location instead.
 */
@Deprecated
public final class IssuePanelPageModuleDescriptor extends RemoteWebPanelModuleDescriptor
{
    public IssuePanelPageModuleDescriptor(
            ModuleFactory moduleFactory,
            IFrameRendererImpl iFrameRenderer,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            HostContainer hostContainer,
            BundleContext bundleContext,
            ConditionProcessor conditionProcessor,
            WebPanelURLParametersSerializer webPanelURLParametersSerializer,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor,
            UrlValidator urlValidator)
    {
        super(moduleFactory, iFrameRenderer, dynamicDescriptorRegistration, hostContainer, bundleContext, conditionProcessor, webPanelURLParametersSerializer, userManager, urlVariableSubstitutor, urlValidator);
    }

    @Override
    protected String getLocation(final Element element)
    {
        return "atl.jira.view.issue.right.context";
    }
}
