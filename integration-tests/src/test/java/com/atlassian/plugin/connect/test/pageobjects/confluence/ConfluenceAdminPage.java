package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.AdminPage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
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
    private final String addOnKey;
    private final String moduleKey;

    private PageElement linkElement;


    public ConfluenceAdminPage(String addOnKey, String moduleKey)
    {
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        findLinkElement().click();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, addOnKey, moduleKey, true);
    }

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
            linkElement = elementFinder.find(By.id(ModuleKeyUtils.addonAndModuleKey(addOnKey, moduleKey)), TimeoutType.DEFAULT);
        }
        return linkElement;
    }

}
