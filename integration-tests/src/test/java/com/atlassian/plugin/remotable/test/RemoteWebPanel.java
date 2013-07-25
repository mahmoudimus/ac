package com.atlassian.plugin.remotable.test;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.pageobjects.elements.WebDriverLocatable;
import com.atlassian.plugin.remotable.pageobjects.RemotePageUtil;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence.PageIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence.SpaceIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.IssueIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.ProfileUserKeyWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.ProfileUserNameWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.ProjectIdWebPanelParameterExtractor;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * A remote web-panel that is expected to contain some test values.
 */
public class RemoteWebPanel extends WebDriverElement
{
    @Inject
    private AtlassianWebDriver driver;

    private final WebDriverLocatable parent;
    private WebElement webPanelContainerDiv;

    public RemoteWebPanel(final By locator, final WebDriverLocatable parent)
    {
        super(locator, parent);
        this.parent = parent;
    }

    @Init
    public void init()
    {
        this.webPanelContainerDiv = driver.findElement(parent.getLocator());
    }

    public String getUserId()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, "user_id");
    }

    public String getProjectId()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, ProjectIdWebPanelParameterExtractor.PROJECT_ID);
    }

    public String getIssueId()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, IssueIdWebPanelParameterExtractor.ISSUE_ID);
    }

    public String getSpaceId()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, SpaceIdWebPanelParameterExtractor.SPACE_ID);
    }

    public String getPageId()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, PageIdWebPanelParameterExtractor.PAGE_ID);
    }

    public String getProfileUserName()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, ProfileUserNameWebPanelParameterExtractor.PROFILE_USER_NAME);
    }

    public String getProfileUserKey()
    {
        return RemotePageUtil.waitForValue(driver, webPanelContainerDiv, ProfileUserKeyWebPanelParameterExtractor.PROFILE_USER_KEY);
    }

}
