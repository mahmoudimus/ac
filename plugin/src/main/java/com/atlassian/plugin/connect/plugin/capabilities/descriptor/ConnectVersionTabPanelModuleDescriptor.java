package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.VersionSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.versiontab.IFrameVersionTab;
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

public class ConnectVersionTabPanelModuleDescriptor extends VersionTabPanelModuleDescriptorImpl
{

    private final IFrameRenderer iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final UrlValidator urlValidator;
    private final ProjectSerializer projectSerializer;
    private final VersionSerializer versionSerializer;

    private String url;
    private Condition condition; // TODO: populate properly
    private IFrameParams iframeParams;


    public ConnectVersionTabPanelModuleDescriptor(ModuleFactory moduleFactory,
                                                IFrameRenderer iFrameRenderer, UrlVariableSubstitutor urlVariableSubstitutor,
                                                JiraAuthenticationContext jiraAuthenticationContext, UrlValidator urlValidator,
                                                ProjectSerializer projectSerializer, VersionSerializer versionSerializer)
    {
        super(jiraAuthenticationContext, moduleFactory);
        this.projectSerializer = checkNotNull(projectSerializer);
        this.versionSerializer = checkNotNull(versionSerializer);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.urlValidator = checkNotNull(urlValidator);
    }

    @Override
    public VersionTabPanel getModule()
    {
        return new IFrameVersionTab(
                new IFrameContextImpl(getPluginKey(), url, key, iframeParams),
                iFrameRenderer, condition, urlVariableSubstitutor, projectSerializer, versionSerializer);
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
