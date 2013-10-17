package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.issuetab.IFrameIssueTab;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Optional;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A ModuleDescriptor for a Connect version of a Jira Issue Tab Panel
 */
public class ConnectIssueTabPanelModuleDescriptor extends IssueTabPanelModuleDescriptorImpl
{

    private final IFrameRenderer iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final UrlValidator urlValidator;
    private final ProjectSerializer projectSerializer;
    private final IssueSerializer issueSerializer;

    private String url;
    private Condition condition; // TODO: populate properly
    private IFrameParamsImpl iframeParams;


    public ConnectIssueTabPanelModuleDescriptor(ModuleFactory moduleFactory,
                                                IFrameRenderer iFrameRenderer, UrlVariableSubstitutor urlVariableSubstitutor,
                                                JiraAuthenticationContext jiraAuthenticationContext, UrlValidator urlValidator,
                                                ProjectSerializer projectSerializer, IssueSerializer issueSerializer)
    {
        super(jiraAuthenticationContext, moduleFactory);
        this.projectSerializer = checkNotNull(projectSerializer);
        this.issueSerializer = checkNotNull(issueSerializer);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.urlValidator = checkNotNull(urlValidator);
    }



    @Override
    public IssueTabPanel3 getModule()
    {
        return new IFrameIssueTab(
                new IFrameContextImpl(getPluginKey() , url, key, iframeParams),
                iFrameRenderer, Optional.fromNullable(condition), urlVariableSubstitutor, projectSerializer, issueSerializer);
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
