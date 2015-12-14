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
public class MovableWebSectionSearcherTest
{
    private final String movableSectionKey1 = "movable-section-1";
    private final String movableSectionKey2 = "movable-section-2";
    private final String jiraSection = "jira-section";

    @Mock
    private PluginAccessor pluginAccessor;

    private MovableWebSectionSearcher movableWebSectionSearcher;

    @Before
    public void setUp() throws Exception
    {
        ModuleFactory moduleFactory = mock(ModuleFactory.class);
        MovableWebPanelLocationProviderModuleDescriptor moduleDescriptor = new MovableWebPanelLocationProviderModuleDescriptor(moduleFactory);
        Plugin plugin = mock(Plugin.class);

        Element element = new BaseElement("movable-web-panel-locations-list");
        element.addAttribute("key", "test-movable-location-provider");
        element.addElement("location").addText(movableSectionKey1);
        element.addElement("location").addText(movableSectionKey2);

        moduleDescriptor.init(plugin, element);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(MovableWebPanelLocationProviderModuleDescriptor.class))
                .thenReturn(ImmutableList.of(moduleDescriptor));

        movableWebSectionSearcher = new MovableWebSectionSearcher(pluginAccessor);
    }

    @Test
    public void shouldDetectThatWebPanelIsInMovableLocationWhenItIsDirectlyInMovableSection()
    {
        WebSectionModuleBean movableWebSection = createWebSectionModuleBean(movableSectionKey1, jiraSection);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(movableSectionKey1);
        ConnectAddonBean connectAddonBean = ConnectAddonBean.newConnectAddonBean().withModules("webSections", movableWebSection).build();

        boolean isWebPanelInMovableWebSection = movableWebSectionSearcher.isWebPanelInMovableWebSection(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInMovableWebSection, is(true));
    }

    @Test
    public void shouldDetectThatWebPanelIsInMovableLocationWhenItIsInSectionThatIsInMovableSection()
    {
        WebSectionModuleBean movableWebSection = createWebSectionModuleBean(movableSectionKey2, jiraSection);
        String sectionKey1 = "section-key-1";
        WebSectionModuleBean webSection1 = createWebSectionModuleBean(sectionKey1, movableSectionKey2);
        String sectionKey2 = "section-key-2";
        WebSectionModuleBean webSection2 = createWebSectionModuleBean(sectionKey2, sectionKey1);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(sectionKey2);
        ConnectAddonBean connectAddonBean = createConnectAddonBean(movableWebSection, webSection1, webSection2);

        boolean isWebPanelInMovableWebSection = movableWebSectionSearcher.isWebPanelInMovableWebSection(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInMovableWebSection, is(true));
    }

    @Test
    public void shouldNotDetectThatWebPanelIsInMovableLocationIfItIsNotInMovableSection()
    {
        String sectionKey1 = "section-key-1";
        WebSectionModuleBean webSection1 = createWebSectionModuleBean(sectionKey1, jiraSection);
        String sectionKey2 = "section-key-2";
        WebSectionModuleBean webSection2 = createWebSectionModuleBean(sectionKey2, sectionKey1);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(sectionKey2);
        ConnectAddonBean connectAddonBean = createConnectAddonBean(webSection1, webSection2);

        boolean isWebPanelInMovableWebSection = movableWebSectionSearcher.isWebPanelInMovableWebSection(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInMovableWebSection, is(false));
    }

    private WebPanelModuleBean createWebPanelModuleBean(String location)
    {
        return WebPanelModuleBean.newWebPanelBean().withLocation(location).build();
    }

    private WebSectionModuleBean createWebSectionModuleBean(String key, String location)
    {
        return WebSectionModuleBean.newWebSectionBean().withLocation(location).withKey(key).build();
    }

    private ConnectAddonBean createConnectAddonBean(WebSectionModuleBean... movableWebSections)
    {
        return ConnectAddonBean.newConnectAddonBean().withModules("webSections", movableWebSections).build();
    }
}