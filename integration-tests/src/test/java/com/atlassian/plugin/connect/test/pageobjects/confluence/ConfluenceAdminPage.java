package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.test.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 *
 */
public class ConfluenceAdminPage implements AdminPage
{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;

    private PageElement linkElement;


    public ConfluenceAdminPage(String pageKey)
    {
        this.pageKey = pageKey;
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

    public String getRemotePluginLinkText()
    {
        return findLinkElement().getText();
    }

    private PageElement findLinkElement()
    {
        if (linkElement == null)
        {
            // pageKey == linkId
            linkElement = elementFinder.find(By.id(pageKey), TimeoutType.DEFAULT);
        }
        return linkElement;
    }

}
