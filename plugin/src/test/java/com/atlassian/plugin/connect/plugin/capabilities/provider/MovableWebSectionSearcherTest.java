package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.spi.web.MovableWebSectionKeysProvider;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MovableWebSectionSearcherTest
{
    private final String movableSectionKey1 = "movable-section-1";
    private final String movableSectionKey2 = "movable-section-2";
    private final String jiraSection = "jira-section";
    private final List<String> movableSectionKeys = ImmutableList.of(movableSectionKey1, movableSectionKey2);

    private final MovableWebSectionKeysProvider movableWebSectionKeysProvider = new MovableWebSectionKeysProvider()
    {
        @Override
        public List<String> provide()
        {
            return movableSectionKeys;
        }
    };

    private final MovableWebSectionSearcher movableWebSectionSearcher = new MovableWebSectionSearcher(movableWebSectionKeysProvider);

    @Test
    public void shouldDetectThatWebPanelIsInMovableSectionWhenItIsDirectlyInMovablebSection()
    {
        WebSectionModuleBean movableWebSection = createWebSectionModuleBean(movableSectionKey1, jiraSection);
        WebPanelModuleBean webPanelModuleBean = createWebPanelModuleBean(movableSectionKey1);
        ConnectAddonBean connectAddonBean = ConnectAddonBean.newConnectAddonBean().withModules("webSections", movableWebSection).build();

        boolean isWebPanelInMovableWebSection = movableWebSectionSearcher.isWebPanelInMovableWebSection(webPanelModuleBean, connectAddonBean);
        assertThat(isWebPanelInMovableWebSection, is(true));
    }

    @Test
    public void shouldDetectThatWebPanelIsInMovableSectionWhenItIsInSecationThatIsInMovableSection()
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
    public void shouldNotDetectThatWebPanelIsInMovableSectionIfItIsNotInMovableSection()
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

    private ConnectAddonBean createConnectAddonBean(WebSectionModuleBean ...movableWebSections)
    {
        return ConnectAddonBean.newConnectAddonBean().withModules("webSections", movableWebSections).build();
    }
}