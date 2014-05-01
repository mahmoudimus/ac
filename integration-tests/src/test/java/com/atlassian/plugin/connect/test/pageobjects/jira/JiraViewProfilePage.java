package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.pageobjects.pages.ViewProfileTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;

import com.google.common.base.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * An user ViewProfile page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewProfilePage extends ViewProfilePage
{
    @ElementBy(id="user_profile_tabs")
    PageElement profileTabs;
    
    final private String profileUsername;

    public JiraViewProfilePage()
    {
        this.profileUsername = null;
    }

    public JiraViewProfilePage(String profileUsername)
    {
        this.profileUsername = profileUsername;
    }

    @Override
    public String getUrl()
    {
        if(null != profileUsername)
        {
            return super.getUrl() + "?name=" + profileUsername;
        }
        else
        {
            return super.getUrl();
        }
    }
    public <T extends ViewProfileTab> T openTab(Class<T> tabClass, String ... args)
    {
        final T tab = pageBinder.delayedBind(tabClass, args).inject().get();
        profileTabs.find(By.id(tab.linkId())).click();
        Poller.waitUntilTrue(tab.isOpen());
        assertCorrectUrl(tab.getUrlPart());
        return tab;
    }

    private void assertCorrectUrl(final String linkPart)
    {
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(final WebDriver webDriver)
            {
                return webDriver.getCurrentUrl().contains(linkPart);
            }
        });
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteWebPanel findWebPanelFromXMLAddOn(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId, "remote-web-panel-");
    }
}
