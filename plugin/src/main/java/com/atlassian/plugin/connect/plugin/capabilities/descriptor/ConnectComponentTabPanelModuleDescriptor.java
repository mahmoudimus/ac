package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.componenttab.IFrameComponentTab;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ModuleDescriptor for Connect component of a ComponentTabPanel
 */
public class ConnectComponentTabPanelModuleDescriptor extends ComponentTabPanelModuleDescriptorImpl
{

    private final IFrameRenderer iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final UrlValidator urlValidator;

    private String url;
    private Condition condition; // TODO: populate properly
    private IFrameParams iframeParams;


    public ConnectComponentTabPanelModuleDescriptor(ModuleFactory moduleFactory,
                                                    IFrameRenderer iFrameRenderer, UrlVariableSubstitutor urlVariableSubstitutor,
                                                    JiraAuthenticationContext jiraAuthenticationContext, UrlValidator urlValidator)
    {
        super(jiraAuthenticationContext, moduleFactory);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.urlValidator = checkNotNull(urlValidator);
    }

    @Override
    public ComponentTabPanel getModule()
    {
        return new IFrameComponentTab(
                new IFrameContextImpl(getPluginKey(), url, key, iframeParams),
                iFrameRenderer, condition, urlVariableSubstitutor);
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
