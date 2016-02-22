package com.atlassian.connect.test.jira.pageobjects;

import java.util.Optional;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteXdmEventPanel;

import org.openqa.selenium.By;

/**
 * A ViewIssue page that is expected to have a panel provided by a remote plugin.
 */
public class ViewIssuePageWithAddonFragments extends ViewIssuePage {
    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder pageElementFinder;

    public ViewIssuePageWithAddonFragments(String issueKey) {
        super(issueKey);
    }

    public RemoteWebPanel findWebPanel(String panelId) {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteXdmEventPanel findXdmEventPanel(String addonId, String moduleId) {
        return pageBinder.bind(RemoteXdmEventPanel.class, addonId, moduleId);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownMenuId) {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownMenuId);
    }

    public boolean isTabPanelPresent(String id) {
        return pageElementFinder.find(By.id(id)).timed().isPresent().byDefaultTimeout();
    }

    public Section getSection(String moduleKey) {
        return pageBinder.bind(Section.class, moduleKey);
    }
}
