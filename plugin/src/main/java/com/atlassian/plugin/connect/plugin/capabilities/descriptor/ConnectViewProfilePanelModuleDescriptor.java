package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.connect.spi.module.IFrameViewProfilePanel;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ModuleDescriptor for Connect version of a ViewProfilePanel
 */
public class ConnectViewProfilePanelModuleDescriptor extends ViewProfilePanelModuleDescriptorImpl
{

    private final IFrameRenderer iFrameRenderer;
    private final UrlValidator urlValidator;

    private String url;
    private IFrameParams iframeParams;


    public ConnectViewProfilePanelModuleDescriptor(ModuleFactory moduleFactory, IFrameRenderer iFrameRenderer,
                                                   JiraAuthenticationContext jiraAuthenticationContext, UrlValidator urlValidator)
    {
        super(jiraAuthenticationContext, moduleFactory);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlValidator = checkNotNull(urlValidator);
    }

    @Override
    public ViewProfilePanel getModule()
    {
        return new IFrameViewProfilePanel(iFrameRenderer, new IFrameContextImpl(getPluginKey(), url, key, iframeParams));
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        this.url = getRequiredAttribute(element, "url");
        urlValidator.validate(this.url);

        iframeParams = new IFrameParamsImpl(element);
    }

    public String getUrl()
    {
        return url;
    }

}
