package com.atlassian.plugin.remotable.plugin.module.jira.issuepanel;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.ConditionProcessor;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.webfragment.StringSubstitutor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.RemoteWebPanelModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelURLParametersSerializer;
import com.atlassian.sal.api.user.UserManager;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

/**
 * A view issue panel page that loads is contents from an iframe.
 * @deprecated use {@link com.atlassian.plugin.remotable.plugin.module.webpanel.RemoteWebPanelModuleDescriptor} with
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
            StringSubstitutor stringSubstitutor)
    {
        super(moduleFactory, iFrameRenderer, dynamicDescriptorRegistration, hostContainer, bundleContext, conditionProcessor, webPanelURLParametersSerializer, userManager, stringSubstitutor);
    }

    @Override
    protected String getLocation(final Element element)
    {
        return "atl.jira.view.issue.right.context";
    }
}
