package com.atlassian.plugin.connect.plugin.web.panel;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class RedirectedWebPanelSectionSearcherTest
{
    private final String redirectedSectionKey1 = "redirected-section-1";
    private final String sectionKey2 = "regular-section-2";
    private final String jiraSection = "jira-section";

    @Mock
    private PluginAccessor pluginAccessor;

    private RedirectedWebPanelSectionSearcher redirectedWebPanelSectionSearcher;

    @Before
    public void setUp() throws Exception
    {
        ModuleFactory moduleFactory = mock(ModuleFactory.class);
        RedirectedWebPanelLocationProviderModuleDescriptor moduleDescriptor = new RedirectedWebPanelLocationProviderModuleDescriptor(moduleFactory);
        Plugin plugin = mock(Plugin.class);

        Element element = new BaseElement("redirected-web-panel-locations-list");
        element.addAttribute("key", "test-redirected-location-provider");
        element.addElement("location").addText(redirectedSectionKey1);
        element.addElement("location").addText(sectionKey2);

        moduleDescriptor.init(plugin, element);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(RedirectedWebPanelLocationProviderModuleDescriptor.class))
                .thenReturn(ImmutableList.of(moduleDescriptor));

        redirectedWebPanelSectionSearcher = new RedirectedWebPanelSectionSearcher(pluginAccessor);
    }

    @Test
    public void shouldDetectThatWebPanelIsInRedirectedLocationWhenItIsDirectlyInRedirectedSection()
    {
        WebSectionModuleBean redirectedWebSection = createWebSectionModuleBean(redirectedSectionKey1, jiraSection);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(redirectedSectionKey1);
        ConnectAddonBean connectAddonBean = ConnectAddonBean.newConnectAddonBean().withModules("webSections", redirectedWebSection).build();

        boolean isWebPanelInRedirectedWebSection = redirectedWebPanelSectionSearcher.doesWebPanelNeedsToBeRedirected(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInRedirectedWebSection, is(true));
    }

    @Test
    public void shouldDetectThatWebPanelIsInRedirectedLocationWhenItIsInSectionThatIsInRedirectedSection()
    {
        WebSectionModuleBean webSection = createWebSectionModuleBean(sectionKey2, jiraSection);
        String sectionKey1 = "section-key-1";
        WebSectionModuleBean webSection1 = createWebSectionModuleBean(sectionKey1, sectionKey2);
        String sectionKey2 = "section-key-2";
        WebSectionModuleBean webSection2 = createWebSectionModuleBean(sectionKey2, sectionKey1);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(sectionKey2);
        ConnectAddonBean connectAddonBean = createConnectAddonBean(webSection, webSection1, webSection2);

        boolean isWebPanelInRedirectedWebSection = redirectedWebPanelSectionSearcher.doesWebPanelNeedsToBeRedirected(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInRedirectedWebSection, is(true));
    }

    @Test
    public void shouldNotDetectThatWebPanelIsInRedirectedLocationIfItIsNotInRedirectedSection()
    {
        String sectionKey1 = "section-key-1";
        WebSectionModuleBean webSection1 = createWebSectionModuleBean(sectionKey1, jiraSection);
        String sectionKey2 = "section-key-2";
        WebSectionModuleBean webSection2 = createWebSectionModuleBean(sectionKey2, sectionKey1);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(sectionKey2);
        ConnectAddonBean connectAddonBean = createConnectAddonBean(webSection1, webSection2);

        boolean isWebPanelInRedirectedWebSection = redirectedWebPanelSectionSearcher.doesWebPanelNeedsToBeRedirected(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInRedirectedWebSection, is(false));
    }

    private WebPanelModuleBean createWebPanelModuleBean(String location)
    {
        return WebPanelModuleBean.newWebPanelBean().withLocation(location).build();
    }

    private WebSectionModuleBean createWebSectionModuleBean(String key, String location)
    {
        return WebSectionModuleBean.newWebSectionBean().withLocation(location).withKey(key).build();
    }

    private ConnectAddonBean createConnectAddonBean(WebSectionModuleBean... webSections)
    {
        return ConnectAddonBean.newConnectAddonBean().withModules("webSections", webSections).build();
    }
}