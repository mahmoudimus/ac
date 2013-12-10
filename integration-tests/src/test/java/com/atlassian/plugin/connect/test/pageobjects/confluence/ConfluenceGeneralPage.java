package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

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

    private WebElement browseMenuLink;
    private PageElement linkElement;


    public ConfluenceGeneralPage(String pageKey, String linkText)
    {
        this(pageKey, linkText, false);
    }

    public ConfluenceGeneralPage(String pageKey, String linkText, boolean ignoreBrowseMenu)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
        this.ignoreBrowseMenu = ignoreBrowseMenu;
    }

    @Init
    @SuppressWarnings("unused")
    public void init()
    {
        By browseLocator = By.id("browse-menu-link");
        if (!ignoreBrowseMenu && driver.elementExists(browseLocator))
        {
            browseMenuLink = driver.findElement(browseLocator);
        }
        else
        {
            browseMenuLink = driver.findElement(By.id("help-menu-link"));
        }
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return findLinkElement().isPresent();
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        findLinkElement().click();
        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
    }

    @Override
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
        return linkElement;
    }

}
