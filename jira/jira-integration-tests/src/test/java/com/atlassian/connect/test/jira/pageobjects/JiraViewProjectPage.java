package com.atlassian.connect.test.jira.pageobjects;

import java.util.Optional;

import javax.inject.Inject;

import com.atlassian.jira.projects.pageobjects.webdriver.page.SummaryPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;

import org.openqa.selenium.By;

/**
 * View project page.
 */
public class JiraViewProjectPage extends SummaryPage implements Page {

    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;

    public JiraViewProjectPage(String projectKey) {
        super(projectKey);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownMenuId) {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownMenuId);
    }

    public Boolean webItemDoesNotExist(String webItemId) {
        return !driver.elementExists(By.id(webItemId));
    }
}
