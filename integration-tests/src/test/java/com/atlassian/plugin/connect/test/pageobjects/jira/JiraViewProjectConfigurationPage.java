package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.webdriver.utils.Check;
import com.google.common.base.Optional;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * View project configuration page.
 */
public class JiraViewProjectConfigurationPage implements Page
{
    private String projectKey;

    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;

    public JiraViewProjectConfigurationPage(String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey;
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownMenuId)
    {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownMenuId);
    }

    public Boolean webItemDoesNotExist(String webItemId)
    {
        return !Check.elementExists(By.id(webItemId), driver);
    }
}
