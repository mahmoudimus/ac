package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 *
 */
public class ConfluenceGeneralPage implements GeneralPage

{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;
    private final boolean ignoreBrowseMenu;

    private PageElement browseMenuLink;
    private PageElement linkElement;
    private final String extraPrefix;


    public ConfluenceGeneralPage(String pageKey, String linkText)
    {
        this(pageKey, linkText, false, "");
    }

    public ConfluenceGeneralPage(String pageKey, String linkText, boolean ignoreBrowseMenu)
    {
        this(pageKey,linkText,ignoreBrowseMenu,"");
    }

    public ConfluenceGeneralPage(String pageKey, String linkText, String extraPrefix)
    {
        this(pageKey, linkText, false, extraPrefix);
    }
    
    public ConfluenceGeneralPage(String pageKey, String linkText, boolean ignoreBrowseMenu, String extraPrefix)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
        this.ignoreBrowseMenu = ignoreBrowseMenu;
        this.extraPrefix = extraPrefix;
    }

    @Init
    @SuppressWarnings("unused")
    public void init()
    {
        By browseLocator = By.id("browse-menu-link");
        if (ignoreBrowseMenu || !driver.elementExists(browseLocator))
        {
            browseLocator = By.id("help-menu-link");
        }

        browseMenuLink = elementFinder.find(browseLocator);
        waitUntilTrue(browseMenuLink.timed().isVisible());
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return findLinkElement().isPresent();
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        findLinkElement().click();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, extraPrefix, pageKey, true);
    }

    public String getRemotePluginLinkHref()
    {
        return findLinkElement().getAttribute("href");
    }

    private PageElement findLinkElement()
    {
        if (linkElement == null)
        {
            browseMenuLink.click();

            linkElement = elementFinder.find(By.linkText(linkText), TimeoutType.DEFAULT);
        }
        else if (!linkElement.isVisible())
        {
            // this opens the drop-down that contains the web item links
            // (if the drop-down isn't open then the links aren't visible, and if they're not visible then they're not clickable)
            browseMenuLink.click();
        }

        return linkElement;
    }

}
