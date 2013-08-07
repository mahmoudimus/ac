package com.atlassian.plugin.connect.plugin.module.jira.projectconfig;

import com.atlassian.plugin.PluginParseException;
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

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;

/**
 * A remote project configuration web panel that loads its contents from an iframe.
 * @deprecated use {@link com.atlassian.plugin.remotable.plugin.module.webpanel.RemoteWebPanelModuleDescriptor} with
 * "webpanels.admin.summary.left-panels" or "webpanels.admin.summary.right-panels" location instead.
 */
@Deprecated
public final class ProjectConfigWebPanelModuleDescriptor extends RemoteWebPanelModuleDescriptor
{
    private static final String PROJECT_WEB_PANEL_LOCATION = "webpanels.admin.summary";

    public ProjectConfigWebPanelModuleDescriptor(
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

    protected String getLocation(final Element element)
    {
        final String location = getOptionalAttribute(element, "location", null);
        if (StringUtils.isEmpty(location) || location.equals("left"))
        {
            return PROJECT_WEB_PANEL_LOCATION + ".left-panels";
        }
        else if (location.equals("right"))
        {
            return PROJECT_WEB_PANEL_LOCATION + ".right-panels";
        }
        else
        {
            throw new PluginParseException("project-config-panel is missing valid location.");
        }
    }
}
