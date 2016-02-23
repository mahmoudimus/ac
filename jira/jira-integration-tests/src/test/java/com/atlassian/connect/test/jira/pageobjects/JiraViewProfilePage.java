package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.pageobjects.pages.ViewProfileTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import org.openqa.selenium.By;

/**
 * An user ViewProfile page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewProfilePage extends ViewProfilePage {
    @ElementBy(id = "user_profile_tabs")
    PageElement profileTabs;

    final private String profileUsername;

    public JiraViewProfilePage() {
        this.profileUsername = null;
    }

    public JiraViewProfilePage(String profileUsername) {
        this.profileUsername = profileUsername;
    }

    @Override
    public String getUrl() {
        if (null != profileUsername) {
            return super.getUrl() + "?name=" + profileUsername;
        } else {
            return super.getUrl();
        }
    }

    public <T extends ViewProfileTab> T openTab(Class<T> tabClass, String... args) {
        final T tab = pageBinder.delayedBind(tabClass, args).inject().get();
        profileTabs.find(By.id(tab.linkId())).click();
        Poller.waitUntilTrue(tab.isOpen());
        assertCorrectUrl(tab.getUrlPart());
        return tab;
    }

    private void assertCorrectUrl(final String linkPart) {
        driver.waitUntil(webDriver -> webDriver.getCurrentUrl().contains(linkPart));
    }

    public RemoteWebPanel findWebPanel(String panelId) {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

}
