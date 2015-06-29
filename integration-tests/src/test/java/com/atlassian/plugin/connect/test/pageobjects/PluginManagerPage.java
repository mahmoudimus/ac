package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

public class PluginManagerPage implements Page
{
    @Inject
    AtlassianWebDriver driver;

    @Inject
    PageBinder pageBinder;

    @Inject
    ProductInstance productInstance;

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/upm";
    }

    @WaitUntil
    public void waitForLoading()
    {
        if (driver.elementExists(By.id("upm-manage-type")))
        {
            driver.navigate().to(productInstance.getBaseUrl() + "/plugins/servlet/upm/manage/user-installed#manage");
            WebDriverPoller poller = new WebDriverPoller(driver);
            poller.waitUntil(ElementConditions.isVisible(By.id("upm-manage-user-installed-plugins")), 60);
            // the list of add-ons loads asynchronously and takes some seconds; wait until at least one is present
            poller.waitUntil(ElementConditions.isVisible(By.className("upm-plugin-name")), 60);
        }
        else
        {
            driver.waitUntilElementIsLocated(By.id("upm-current-plugins"));
            WebElement userPlugins = driver.findElement(By.id("upm-current-plugins"));
            driver.waitUntilElementIsNotLocatedAt(By.className("loading"), userPlugins);
        }
    }
    
    private void clickPluginButton(String pluginKeyAndName, String pageKey, String buttonText)
    {
        for (WebElement element : driver.findElements(By.className("upm-plugin-name")))
        {
            if (element.getText().trim().equals(pluginKeyAndName))
            {
                element.click();
                By byButton = By.linkText(buttonText);
                driver.waitUntilElementIsVisible(byButton);
                WebElement configureLink = driver.findElement(byButton);
                if (configureLink.getAttribute("href").endsWith(
                        pluginKeyAndName + "/" + pageKey))
                {
                    configureLink.click();
                    return;
                }
            }
        }
        throw new IllegalStateException("Didn't find plugin " + pluginKeyAndName);
        
    }

    public void clickConfigurePluginButton(String pluginKeyAndName, String pageKey)
    {
        clickPluginButton(pluginKeyAndName, pageKey, "Configure");
    }

    public void clickGetStartedPluginButton(String pluginKeyAndName, String pageKey)
    {
        clickPluginButton(pluginKeyAndName, pageKey, "Get Started");
    }
}
