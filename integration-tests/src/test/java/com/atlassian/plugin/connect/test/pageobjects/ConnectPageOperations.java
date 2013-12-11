package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.ID;

/**
 * TODO: thinking this may be a better approach than base pages as we can extend the actual product page objects and
 * add this in like (a poor mans) mixin. Also it allows us to share these helper methods between products
 */
public class ConnectPageOperations
{
    private PageBinder pageBinder;

    private AtlassianWebDriver driver;

    @Inject
    public ConnectPageOperations(PageBinder pageBinder, AtlassianWebDriver driver)
    {
        this.pageBinder = pageBinder;
        this.driver = driver;
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
//        Check.elementExists(By.id(webItemId));
        return !driver.elementExists(By.id(webItemId));
    }

    public LinkedRemoteContent findConnectPage(RemoteWebItem.ItemMatchingMode mode, String linkText, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(mode, linkText, dropDownMenuId, "servlet-" + pageKey);
    }

    public LinkedRemoteContent findTabPanel(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findConnectPage(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, "servlet-" + pageKey);
    }

    private LinkedRemoteContent findRemoteLinkedContent(RemoteWebItem.ItemMatchingMode mode, String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return pageBinder.bind(LinkedRemoteContent.class, mode, webItemId, dropDownMenuId, pageKey);
    }

    private LinkedRemoteContent findRemoteLinkedContent(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(ID, webItemId, dropDownMenuId, pageKey);
    }

}
