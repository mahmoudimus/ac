package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import org.openqa.selenium.By;

/**
 * Page for edit of confluence pages.
 */
public class ConfluenceEditPage implements Page
{
    private final String pageId;

    @Inject
    private PageBinder pageBinder;

    @javax.inject.Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

    public ConfluenceEditPage(final String pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String getUrl()
    {
        return "/pages/editpage.action?pageId=" + pageId;
    }

    public RemoteWebPanel findWebPanel(String id)
    {
        return pageBinder.bind(RemoteWebPanel.class, id);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownLinkId)
    {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownLinkId);
    }

    public Boolean webItemDoesNotExist(String webItemId)
    {
        return !driver.elementExists(By.id(webItemId));
    }
}
