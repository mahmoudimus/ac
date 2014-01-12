package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
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
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectComponentTabPanelModuleDescriptorTest extends AbstractConnectTabPanelModuleDescriptorTest<ComponentTabPanel>
{
    private static final String ADDON_HTML_CONTENT = "the content goes here";
    private static final String ADDON_NAME = "My Component Tab Page";
    private static final String ADDON_URL = "http://blah?my_project_id={project.id}&my_project_key={project.key}";
    private static final String ADDON_KEY = "my-component-tab-page";
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
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private UrlValidator urlValidator;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ProjectComponent component;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Project project;

    @Mock
    private User user;

    @Mock
    private I18nHelper i18nHelper;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private BrowseComponentContext browseComponentContext;


    @Test
    public void populatesDescriptorPropertiesFromElement() throws IOException
    {
        ConnectComponentTabPanelModuleDescriptor descriptor = createDescriptor();

        assertThat(descriptor.getCompleteKey(), is(equalTo(PLUGIN_KEY + ":" + ADDON_KEY)));
        assertThat(descriptor.getName(), is(equalTo(ADDON_NAME)));
        assertThat(descriptor.getUrl(), is(equalTo(ADDON_URL)));
        assertThat(descriptor.getOrder(), is(equalTo(99)));
        assertThat(descriptor.getLabel(), is(equalTo(ADDON_I18_NAME)));
        assertThat(descriptor.getModuleClass(), is(equalTo(ComponentTabPanel.class)));
    }

    @Test
    public void createdModuleReturnsAddOnContentForHTML() throws IOException
    {
        ConnectComponentTabPanelModuleDescriptor descriptor = createDescriptor();

        ComponentTabPanel module = descriptor.getModule();

        assertThat(module.getHtml(browseComponentContext), is(equalTo(ADDON_HTML_CONTENT)));
    }

    @Override
    protected ConnectComponentTabPanelModuleDescriptor createDescriptor() throws IOException
    {
        when(iFrameRenderer.render(any(IFrameContext.class), anyString())).thenReturn(ADDON_HTML_CONTENT);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(ADDON_LABEL_KEY)).thenReturn(ADDON_I18_NAME);
        when(browseComponentContext.getContextKey()).thenReturn(ADDON_KEY);
        when(browseComponentContext.getComponent()).thenReturn(component);
        when(browseComponentContext.getProject()).thenReturn(project);
        when(project.getKey()).thenReturn("42"); // not sure why the deep stubs aren't mocking this

        ConnectComponentTabPanelModuleDescriptor descriptor = new ConnectComponentTabPanelModuleDescriptor(moduleFactory,
                iFrameRenderer, new UrlVariableSubstitutor(), jiraAuthenticationContext, urlValidator);
        descriptor.init(PLUGIN, ISSUE_TAB_PAGE_ELEMENT);
        descriptor.enabled();
        return descriptor;
    }

    @Override
    protected IFrameRenderer getIFrameRenderer()
    {
        return iFrameRenderer;
    }

    @Override
    protected String getRawUrl()
    {
        return ADDON_URL;
    }

    private static Element createElement()
    {
        Element componentTabPageElement = new DOMElement("component-tab-page");
        componentTabPageElement.addAttribute("key", ADDON_KEY);
        componentTabPageElement.addElement("order").setText("99");
        componentTabPageElement.addAttribute("url", ADDON_URL);
        componentTabPageElement.addAttribute("name", ADDON_NAME);
        componentTabPageElement.addAttribute("class", ComponentTabPanel.class.getName());
        componentTabPageElement.addElement("label")
                .addAttribute("key", ADDON_LABEL_KEY)
                .setText(ADDON_I18_NAME);
        return componentTabPageElement;
    }

}
