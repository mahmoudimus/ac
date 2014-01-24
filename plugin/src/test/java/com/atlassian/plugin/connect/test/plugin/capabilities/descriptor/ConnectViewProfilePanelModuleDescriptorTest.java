package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.IFrameContextMatchers;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectViewProfilePanelModuleDescriptorTest
{
    private static final String ADDON_HTML_CONTENT = "the content goes here";
    private static final String ADDON_NAME = "My ViewProfile Tab Page";
    private static final String ADDON_URL = "http://blah";
    private static final String ADDON_KEY = "my-view-profile-tab-page";
    private static final String ADDON_I18_NAME = "My Plugin i18";
    private static final Element TAB_PAGE_ELEMENT = createElement();
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
    private User user;
    @Mock
    private I18nHelper i18nHelper;


    @Test
    public void populatesDescriptorPropertiesFromElement() throws IOException
    {
        ConnectViewProfilePanelModuleDescriptor descriptor = createDescriptor();

        assertThat(descriptor.getCompleteKey(), is(equalTo(PLUGIN_KEY + ":" + ADDON_KEY)));
        assertThat(descriptor.getName(), is(equalTo(ADDON_NAME)));
        assertThat(descriptor.getUrl(), is(equalTo(ADDON_URL)));
        assertThat(descriptor.getOrder(), is(equalTo(100)));
        assertThat(descriptor.getModuleClass(), is(equalTo(ViewProfilePanel.class)));
    }

    @Test
    public void createdModuleReturnsAddOnContentForHTML() throws IOException
    {
        ConnectViewProfilePanelModuleDescriptor descriptor = createDescriptor();

        ViewProfilePanel module = descriptor.getModule();

        assertThat(module.getHtml(user), is(equalTo(ADDON_HTML_CONTENT)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rendersWithCorrectUrl() throws IOException
    {
        createDescriptor().getModule().getHtml(user);
        verify(iFrameRenderer).render(argThat(IFrameContextMatchers.hasIFramePath(ADDON_URL)), anyString());
    }


    protected ConnectViewProfilePanelModuleDescriptor createDescriptor() throws IOException
    {
        when(iFrameRenderer.render(any(IFrameContext.class), anyString())).thenReturn(ADDON_HTML_CONTENT);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(ADDON_LABEL_KEY)).thenReturn(ADDON_I18_NAME);

        ConnectViewProfilePanelModuleDescriptor descriptor = new ConnectViewProfilePanelModuleDescriptor(moduleFactory,
                iFrameRenderer, jiraAuthenticationContext, urlValidator);
        descriptor.init(PLUGIN, TAB_PAGE_ELEMENT);
        descriptor.enabled();
        return descriptor;
    }

    private static Element createElement()
    {
        Element viewProfileTabPageElement = new DOMElement("view-profile-tab-page");
        viewProfileTabPageElement.addAttribute("key", ADDON_KEY);
        viewProfileTabPageElement.addElement("order").setText("100");
        viewProfileTabPageElement.addAttribute("url", ADDON_URL);
        viewProfileTabPageElement.addAttribute("name", ADDON_NAME);
        viewProfileTabPageElement.addAttribute("class", ViewProfilePanel.class.getName());
        viewProfileTabPageElement.addElement("label")
                .addAttribute("key", ADDON_LABEL_KEY)
                .setText(ADDON_I18_NAME);
        return viewProfileTabPageElement;
    }

}
