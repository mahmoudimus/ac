package com.atlassian.plugin.connect.test.common.pageobjects;

import java.util.Optional;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.atlassian.webdriver.utils.element.WebDriverPoller;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

/**
 * The set of standard operations for testing connect addons. The intention is to have one place for all such ops that
 * are separate from the page objects. This allows us to avoid copy pasting these and more importantly hopefully avoids
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

    public <T extends RemoteWebPanel> T findWebPanel(String id, Class<T> panelClass)
    {
        return pageBinder.bind(panelClass, id);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownLinkId)
    {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownLinkId);
    }

    public RemoteWebItem findWebItem(ItemMatchingMode mode, String matchValue, Optional<String> dropDownLinkId)
    {
        return pageBinder.bind(RemoteWebItem.class, mode, matchValue, dropDownLinkId);
    }

    public RenderedMacro findMacroWithIdPrefix(String idPrefix)
    {
        return pageBinder.bind(RenderedMacro.class, idPrefix);
    }

    public RenderedMacro findMacroWithIdPrefix(String idPrefix, int indexOnPage)
    {
        return pageBinder.bind(RenderedMacro.class, idPrefix, indexOnPage);
    }

    public void waitUntilNConnectIFramesPresent(final int n)
    {
        new WebDriverPoller(driver).waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(final WebDriver input)
            {
                return n == size(filter(input.findElements(By.tagName("iframe")), new Predicate<WebElement>()
                {
                    @Override
                    public boolean apply(final WebElement input)
                    {
                        String id = input.getAttribute("id");
                        return id != null && id.startsWith("easyXDM_embedded-");
                    }
                }));
            }
        });
    }

    public boolean existsWebItem(String webItemId)
    {
        return existsElementWithId(webItemId);
    }

    public boolean existsWebPanel(String webPanelId)
    {
        return existsElementWithId(IframeUtils.iframeId(webPanelId));
    }

    private boolean existsElementWithId(final String id)
    {
        return driver.elementExists(By.id(id));
    }

    public LinkedRemoteContent findConnectPage(ItemMatchingMode mode, String linkText, Optional<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(mode, linkText, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findTabPanel(String webItemId, Optional<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findRemoteLinkedContent(ItemMatchingMode mode, String webItemId, Optional<String> dropDownMenuId, String pageKey)
    {
        return pageBinder.bind(LinkedRemoteContent.class, mode, webItemId, dropDownMenuId, pageKey);
    }

    private LinkedRemoteContent findRemoteLinkedContent(String webItemId, Optional<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(ItemMatchingMode.ID, webItemId, dropDownMenuId, pageKey);
    }

    public RemotePluginDialog findDialog(String moduleKey)
    {
        return findDialog(moduleKey, RemotePluginDialog.class);
    }

    public <T extends RemotePluginDialog> T findDialog(String moduleKey, Class<T> dialogClass)
    {
        ConnectAddOnEmbeddedTestPage dialogContent = pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, null, moduleKey, true);
        return pageBinder.bind(dialogClass, dialogContent);
    }

    public WebElement findLabel(String key)
    {
        String escapedKey = AddonTestUtils.escapeJQuerySelector(key);
        return driver.findElement(ByJquery.$("label[for='" + escapedKey + "']"));
    }

    public PageBinder getPageBinder()
    {
        return pageBinder;
    }

    public RemotePluginDialog editMacro(String macroKey)
    {
        String macroNodeSelector = "$(\"#wysiwygTextarea_ifr\").contents().find(\"table[data-macro-name='" + macroKey + "']\")";
        driver.executeScript("tinymce.confluence.macrobrowser.editMacro(" + macroNodeSelector + ")");
        return findDialog(macroKey);
    }

    public void reorderConfluenceTableOnPage()
    {
        driver.findElement(By.className("tablesorter-header-inner")).click();
    }

    public WebElement findElement(By by)
    {
        return driver.findElement(by);
    }

    public WebElement findElementByClass(String className)
    {
        return findElement(By.className(className));
    }
}