package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel3;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
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
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.TestMatchers.hasIFramePath;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectIssueTabPanelModuleDescriptorTest
{
    private static final String ADDON_HTML_CONTENT = "the content goes here";
    private static final String ADDON_NAME = "My Issue Tab Page";
    private static final String ADDON_URL = "http://blah?my_issue_key=${issue.key}";
    private static final String ADDON_KEY = "my-issue-tab-page";
    private static final String ADDON_I18_NAME = "My Plugin i18";
    private static final Element ISSUE_TAB_PAGE_ELEMENT = createElement();
    private static final String PLUGIN_KEY = "my-key";
    private static final String ADDON_LABEL_KEY = "My Plugin";
    private static final Plugin PLUGIN = new PluginForTests(PLUGIN_KEY, ADDON_LABEL_KEY);
    private static final String ISSUE_KEY = "ABC-123";

    @Mock
    private ModuleFactory moduleFactory;

    @Mock
    private IFrameRenderer iFrameRenderer;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private UrlValidator urlValidator;
    @Mock
    private ProjectSerializer projectSerializer;
    @Mock
    private Issue issue;
    @Mock
    private User user;
    @Mock
    private I18nHelper i18nHelper;


    @Test
    public void createsDescriptorWithCorrectCompleteKey() throws IOException
    {
        assertThat(createDescriptor().getCompleteKey(), is(equalTo(PLUGIN_KEY + ":" + ADDON_KEY)));
    }

    @Test
    public void createsDescriptorWithCorrectName() throws IOException
    {
        assertThat(createDescriptor().getName(), is(equalTo(ADDON_NAME)));
    }

    @Test
    public void createsDescriptorWithCorrectUrl() throws IOException
    {
        assertThat(createDescriptor().getUrl(), is(equalTo(ADDON_URL)));
    }

    @Test
    public void createsDescriptorWithCorrectOrder() throws IOException
    {
        assertThat(createDescriptor().getOrder(), is(equalTo(99)));
    }

    @Test
    public void createsDescriptorWithCorrectLabel() throws IOException
    {
        assertThat(createDescriptor().getLabel(), is(equalTo(ADDON_I18_NAME)));
    }


    @Test
    public void createdModuleReturnsAddOnContentForHTML() throws IOException
    {
        ConnectIssueTabPanelModuleDescriptor descriptor = createDescriptor();

        IssueTabPanel3 module = descriptor.getModule();

        List<IssueAction> actions = module.getActions(new GetActionsRequest(issue, user, false, false, null));
        assertThat(actions, Matchers.<IssueAction>contains(hasProperty("html", equalTo(ADDON_HTML_CONTENT))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rendersWithCorrectUrl() throws IOException
    {
        List<IssueAction> actions = createDescriptor().getModule().getActions(new GetActionsRequest(issue, mock(User.class), true, true, ""));
        actions.get(0).getHtml();
        String expectedUrl = ADDON_URL.replace("${issue.key}", ISSUE_KEY);
        verify(iFrameRenderer).render(argThat(hasIFramePath(expectedUrl)), anyString());
    }

    private ConnectIssueTabPanelModuleDescriptor createDescriptor() throws IOException
    {
        when(projectSerializer.serialize(any(Project.class))).thenReturn(ImmutableMap.<String, Object>of());
        when(iFrameRenderer.render(any(IFrameContext.class), anyString())).thenReturn(ADDON_HTML_CONTENT);
        when(issue.getKey()).thenReturn(ISSUE_KEY);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(ADDON_LABEL_KEY)).thenReturn(ADDON_I18_NAME);

        ConnectIssueTabPanelModuleDescriptor descriptor = new ConnectIssueTabPanelModuleDescriptor(moduleFactory,
                iFrameRenderer, new UrlVariableSubstitutor(), jiraAuthenticationContext, urlValidator, projectSerializer,
                new IssueSerializer());
        descriptor.init(PLUGIN, ISSUE_TAB_PAGE_ELEMENT);

        return descriptor;
    }

    private static Element createElement()
    {
        Element issueTabPageElement = new DOMElement("issue-tab-page");
        issueTabPageElement.addAttribute("key", ADDON_KEY);
        issueTabPageElement.addElement("order").setText("99");
        issueTabPageElement.addAttribute("url", ADDON_URL);
        issueTabPageElement.addAttribute("name", ADDON_NAME);
        issueTabPageElement.addElement("label")
                .addAttribute("key", ADDON_LABEL_KEY)
                .setText(ADDON_I18_NAME);
        return issueTabPageElement;
    }

}
