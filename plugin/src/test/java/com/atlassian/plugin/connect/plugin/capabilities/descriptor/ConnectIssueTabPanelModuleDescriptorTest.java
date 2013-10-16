package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel3;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.IssueSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectIssueTabPanelModuleDescriptorTest
{
    @Mock
    private ModuleFactory moduleFactory;

    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private UrlValidator urlValidator;
    @Mock
    private ProjectSerializer projectSerializer;
    @Mock
    private IssueSerializer issueSerializer;
    @Mock
    private Issue issue;
    @Mock
    private User user;


    @Test
    public void blah() throws IOException
    {
        Plugin plugin = new PluginForTests("my-key", "My Plugin");


        ConnectIssueTabPanelModuleDescriptor descriptor = new ConnectIssueTabPanelModuleDescriptor(moduleFactory,
                iFrameRenderer, urlVariableSubstitutor, jiraAuthenticationContext, urlValidator, projectSerializer,
                issueSerializer);

        Element issueTabPageElement = new DOMElement("issue-tab-page");
        issueTabPageElement.addAttribute("key", "my-issue-tab-page");
        issueTabPageElement.addAttribute("weight", "100");
        issueTabPageElement.addAttribute("url", "http://blah");
        issueTabPageElement.addAttribute("name", "My Issue Tab Page");
        issueTabPageElement.addElement("label")
                .addAttribute("key", "My Plugin")
                .setText("My Plugin i18");

        descriptor.init(plugin, issueTabPageElement);

        assertThat(descriptor.getUrl(), is(equalTo("http://blah")));
        IssueTabPanel3 module = descriptor.getModule();

        when(projectSerializer.serialize(any(Project.class))).thenReturn(ImmutableMap.<String, Object>of());
        when(issueSerializer.serialize(any(Issue.class))).thenReturn(ImmutableMap.<String, Object>of());
        GetActionsRequest request = new GetActionsRequest(issue, user, false, false, null);

        when(request.issue().getKey()).thenReturn("ABC-123");
        when(iFrameRenderer.render(any(IFrameContext.class), anyString())).thenReturn("the content goes here");
        assertThat(descriptor.getCompleteKey(), is(equalTo("my-key:my-issue-tab-page")));
        assertThat(descriptor.getName(), is(equalTo("My Issue Tab Page")));

        List<IssueAction> actions = module.getActions(request);
        assertThat(actions, hasSize(1));
        assertThat(actions.get(0).getHtml(), is(equalTo("the content goes here")));
    }
}
