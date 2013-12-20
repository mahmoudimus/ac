package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.capabilities.util.TestContextBuilder;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.VersionSerializer;
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
public class ConnectVersionTabPanelModuleDescriptorTest extends AbstractConnectTabPanelModuleDescriptorTest<VersionTabPanel>
{
    private static final String ADDON_HTML_CONTENT = "the content goes here";
    private static final String ADDON_NAME = "My Version Tab Page";
    private static final String ADDON_URL = "http://blah?my_project_id={project.id}&my_project_key={project.key}";
    private static final String ADDON_KEY = "my-version-tab-page";
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
    @Mock
    private VersionSerializer versionSerializer;
    @Mock
    private Version version;
    @Mock
    private User user;
    @Mock
    private I18nHelper i18nHelper;


    @Test
    public void populatesDescriptorPropertiesFromElement() throws IOException
    {
        ConnectVersionTabPanelModuleDescriptor descriptor = createDescriptor();

        assertThat(descriptor.getCompleteKey(), is(equalTo(PLUGIN_KEY + ":" + ADDON_KEY)));
        assertThat(descriptor.getName(), is(equalTo(ADDON_NAME)));
        assertThat(descriptor.getUrl(), is(equalTo(ADDON_URL)));
        assertThat(descriptor.getOrder(), is(equalTo(100)));
        assertThat(descriptor.getLabel(), is(equalTo(ADDON_I18_NAME)));
        assertThat(descriptor.getModuleClass(), is(equalTo(VersionTabPanel.class)));
    }

    @Test
    public void createdModuleReturnsAddOnContentForHTML() throws IOException
    {
        ConnectVersionTabPanelModuleDescriptor descriptor = createDescriptor();

        VersionTabPanel module = descriptor.getModule();

        assertThat(module.getHtml(TestContextBuilder.buildBrowseVersionContext()), is(equalTo(ADDON_HTML_CONTENT)));
    }

    @Override
    protected ConnectVersionTabPanelModuleDescriptor createDescriptor() throws IOException
    {
        when(versionSerializer.serialize(any(Version.class))).thenReturn(ImmutableMap.<String, Object>of());
        when(iFrameRenderer.render(any(IFrameContext.class), anyString())).thenReturn(ADDON_HTML_CONTENT);
//        when(version.getName()).thenReturn("ABC-123");
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(ADDON_LABEL_KEY)).thenReturn(ADDON_I18_NAME);

        ConnectVersionTabPanelModuleDescriptor descriptor = new ConnectVersionTabPanelModuleDescriptor(moduleFactory,
                iFrameRenderer, new UrlVariableSubstitutor(), jiraAuthenticationContext, urlValidator, new ProjectSerializer(),
                versionSerializer);
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

    @Override
    protected BrowseContext createBrowseContext()
    {
        return TestContextBuilder.buildBrowseVersionContext();
    }

    private static Element createElement()
    {
        Element versionTabPageElement = new DOMElement("version-tab-page");
        versionTabPageElement.addAttribute("key", ADDON_KEY);
        versionTabPageElement.addElement("order").setText("100");
        versionTabPageElement.addAttribute("url", ADDON_URL);
        versionTabPageElement.addAttribute("name", ADDON_NAME);
        versionTabPageElement.addAttribute("class", VersionTabPanel.class.getName());
        versionTabPageElement.addElement("label")
                .addAttribute("key", ADDON_LABEL_KEY)
                .setText(ADDON_I18_NAME);
        return versionTabPageElement;
    }

}
