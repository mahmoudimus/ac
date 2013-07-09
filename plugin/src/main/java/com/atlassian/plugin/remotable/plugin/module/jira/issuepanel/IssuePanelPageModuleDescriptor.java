package com.atlassian.plugin.remotable.plugin.module.jira.issuepanel;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.plugin.module.ConditionProcessor;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.webpanel.RemoteWebPanelModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelAllParametersExtractor;
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
            final ModuleFactory moduleFactory,
            final IFrameRendererImpl iFrameRenderer,
            final DynamicDescriptorRegistration dynamicDescriptorRegistration,
            final HostContainer hostContainer,
            final BundleContext bundleContext,
            final ConditionProcessor conditionProcessor,
            final WebPanelAllParametersExtractor webPanelAllParametersExtractor,
            final UserManager userManager)
    {
        super(moduleFactory, iFrameRenderer, dynamicDescriptorRegistration, hostContainer, bundleContext, conditionProcessor, webPanelAllParametersExtractor, userManager);
    }

    @Override
    protected final String getLocation(final Element element)
    {
        return "atl.jira.view.issue.right.context";
    }
}
