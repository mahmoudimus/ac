package com.atlassian.connect.test.confluence.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;

/**
 *
 */
public class ConfluenceAdminPage implements AdminPage
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;
    private final String addonKey;
    private final String moduleKey;

    private PageElement linkElement;


    public ConfluenceAdminPage(String addonKey, String moduleKey)
    {
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public ConnectAddonEmbeddedTestPage clickAddonLink()
    {
        findLinkElement().click();
        return pageBinder.bind(ConnectAddonEmbeddedTestPage.class, addonKey, moduleKey, true);
    }

    public String getRemotePluginLinkHref()
    {
        return findLinkElement().getAttribute("href");
    }

    public String getRemotePluginLinkText()
    {
        return findLinkElement().getText();
    }

    @Override
    public PageElement findLinkElement()
    {
        if (linkElement == null)
        {
            // pageKey == linkId
            linkElement = elementFinder.find(By.id(ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey)), TimeoutType.DEFAULT);
        }
        return linkElement;
    }

}
