package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.ID;

/**
 * The set of standard operations for testing connect addons. The intention is to have one place for all such ops
 * that is separate from the page objects. This allows us to avoid copy pasting these and more importantly hopefully avoids
 * us needing connect specific page objects. i.e. use the standard product ones unless there is a good reason not to.
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

    public RenderedMacro findMacro(String macroKey, int count)
    {
        return pageBinder.bind(RenderedMacro.class, macroKey, count);
    }

    public Boolean webItemDoesNotExist(String webItemId)
    {
//        Check.elementExists(By.id(webItemId));
        return !driver.elementExists(By.id(webItemId));
    }

    public LinkedRemoteContent findConnectPage(ItemMatchingMode mode, String linkText, Option<String> dropDownMenuId, String pageKey)
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

    private LinkedRemoteContent findRemoteLinkedContent(ItemMatchingMode mode, String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return pageBinder.bind(LinkedRemoteContent.class, mode, webItemId, dropDownMenuId, pageKey);
    }

    private LinkedRemoteContent findRemoteLinkedContent(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(ID, webItemId, dropDownMenuId, pageKey);
    }

    public RemotePluginDialog findDialog(String key)
    {
        RemotePluginTestPage dialogContent = pageBinder.bind(RemotePluginTestPage.class, key);
        return pageBinder.bind(RemotePluginDialog.class, dialogContent);
    }

}
