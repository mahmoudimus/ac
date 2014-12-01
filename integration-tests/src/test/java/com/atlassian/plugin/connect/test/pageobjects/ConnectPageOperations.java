package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConnectMacroBrowserDialog;
import com.atlassian.plugin.connect.test.pageobjects.confluence.RenderedMacro;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.ID;
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

    private static final Logger logger = LoggerFactory.getLogger(ConnectPageOperations.class);

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

    public boolean existsTabPanel(String tabPanelId)
    {
        return existsElementWithId(tabPanelId);
    }

    private boolean existsElementWithId(final String id)
    {
        return driver.elementExists(By.id(id));
    }

    public LinkedRemoteContent findConnectPage(ItemMatchingMode mode, String linkText, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(mode, linkText, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findTabPanel(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findConnectPage(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findRemoteLinkedContent(ItemMatchingMode mode, String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return pageBinder.bind(LinkedRemoteContent.class, mode, webItemId, dropDownMenuId, pageKey);
    }

    public ConnectMacroBrowserDialog findConnectMacroBrowserDialog()
    {
        return pageBinder.bind(ConnectMacroBrowserDialog.class);
    }

    private LinkedRemoteContent findRemoteLinkedContent(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(ID, webItemId, dropDownMenuId, pageKey);
    }

    public RemotePluginDialog findDialog(String moduleKey)
    {
        ConnectAddOnEmbeddedTestPage dialogContent = pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, null, moduleKey, true);
        return pageBinder.bind(RemotePluginDialog.class, dialogContent);
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

    public void dismissAnyAlerts()
    {
        try
        {
            driver.switchTo().alert().dismiss();
            logger.debug("Dismissed an alert.");
        }
        catch (Exception e)
        {
            logger.debug("No alerts to dismiss, or failed to dismiss an alert.");
        }
    }

    public void dismissConfluenceDiscardDraftsPrompt()
    {
        // dismiss a "you have an unsaved draft" message, if any, because it actually blocks the cancel and other buttons
        try
        {
            final WebElement discardLink = findElementByClass("discard-draft");

            if (null != discardLink)
            {
                discardLink.click();
            }
        }
        catch (NoSuchElementException e)
        {
            // don't care
        }
    }

    public void dismissClosableAuiMessage()
    {
        try
        {
            final WebElement message = findElementByClass("aui-message");

            if (null != message && message.isDisplayed())
            {
                final WebElement closeButton = message.findElement(By.className("icon-close"));

                if (null != closeButton)
                {
                    closeButton.click();
                }
            }
        }
        catch (NoSuchElementException e)
        {
            // don't care
        }
    }
}
