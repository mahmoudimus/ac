package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectProjectTabPanelModuleDescriptorTest
{
    private static final String ADDON_HTML_CONTENT = "the content goes here";
    private static final String ADDON_NAME = "My Project Tab Page";
    private static final String ADDON_URL = "http://blah";
    private static final String ADDON_KEY = "my-project-tab-page";
    private static final String ADDON_I18_NAME = "My Plugin i18";
    private static final Element ISSUE_TAB_PAGE_ELEMENT = createElement();
    private static final String PLUGIN_KEY = "my-key";
    private static final String ADDON_LABEL_KEY = "My Plugin";
    private static final Plugin PLUGIN = new PluginForTests(PLUGIN_KEY, ADDON_LABEL_KEY);

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
    private Project project;
    @Mock
    private User user;
    @Mock
    private I18nHelper i18nHelper;
    @Mock
    private BrowseProjectContext browseProjectContext;


    @Test
    public void populatesDescriptorPropertiesFromElement() throws IOException
    {
        ConnectProjectTabPanelModuleDescriptor descriptor = createDescriptor();

        assertThat(descriptor.getCompleteKey(), is(equalTo(PLUGIN_KEY + ":" + ADDON_KEY)));
        assertThat(descriptor.getName(), is(equalTo(ADDON_NAME)));
        assertThat(descriptor.getUrl(), is(equalTo(ADDON_URL)));
        assertThat(descriptor.getOrder(), is(equalTo(100)));
        assertThat(descriptor.getLabel(), is(equalTo(ADDON_I18_NAME)));
        assertThat(descriptor.getModuleClass(), is(equalTo(ProjectTabPanel.class)));
    }

    @Test
    public void createdModuleReturnsAddOnContentForHTML() throws IOException
    {
        ConnectProjectTabPanelModuleDescriptor descriptor = createDescriptor();

        ProjectTabPanel module = descriptor.getModule();

        assertThat(module.getHtml(browseProjectContext), is(equalTo(ADDON_HTML_CONTENT)));
    }

    private ConnectProjectTabPanelModuleDescriptor createDescriptor() throws IOException
    {
        when(projectSerializer.serialize(any(Project.class))).thenReturn(ImmutableMap.<String, Object>of());
        when(projectSerializer.serialize(any(Project.class))).thenReturn(ImmutableMap.<String, Object>of());
        when(iFrameRenderer.render(any(IFrameContext.class), anyString())).thenReturn(ADDON_HTML_CONTENT);
//        when(project.getName()).thenReturn("ABC-123");
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(ADDON_LABEL_KEY)).thenReturn(ADDON_I18_NAME);
        when(browseProjectContext.getContextKey()).thenReturn(ADDON_KEY);
        when(browseProjectContext.getProject()).thenReturn(project);

        ConnectProjectTabPanelModuleDescriptor descriptor = new ConnectProjectTabPanelModuleDescriptor(moduleFactory,
                iFrameRenderer, urlVariableSubstitutor, jiraAuthenticationContext, urlValidator, projectSerializer);
        descriptor.init(PLUGIN, ISSUE_TAB_PAGE_ELEMENT);
        descriptor.enabled();
        return descriptor;
    }

    private static Element createElement()
    {
        Element projectTabPageElement = new DOMElement("project-tab-page");
        projectTabPageElement.addAttribute("key", ADDON_KEY);
        projectTabPageElement.addElement("order").setText("100");
        projectTabPageElement.addAttribute("url", ADDON_URL);
        projectTabPageElement.addAttribute("name", ADDON_NAME);
        projectTabPageElement.addAttribute("class", ProjectTabPanel.class.getName());
        projectTabPageElement.addElement("label")
                .addAttribute("key", ADDON_LABEL_KEY)
                .setText(ADDON_I18_NAME);
        return projectTabPageElement;
    }

}
