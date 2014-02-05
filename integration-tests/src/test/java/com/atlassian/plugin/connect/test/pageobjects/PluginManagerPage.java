package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
            driver.waitUntilElementIsVisible(By.id("upm-manage-user-installed-plugins"));
            
            //added this to stop flakiness due to not waiting for the plugins list to be loaded
            driver.waitUntilElementIsVisible(By.className("upm-plugin-list-container"));
        }
        else
        {
            driver.waitUntilElementIsLocated(By.id("upm-current-plugins"));
            WebElement userPlugins = driver.findElement(By.id("upm-current-plugins"));
            driver.waitUntilElementIsNotLocatedAt(By.className("loading"), userPlugins);
        }
    }

    public <P extends Object> P configurePlugin(String pluginKeyAndName, String pageKey, Class<P> nextPage, String extraPrefix)
    {
        for (WebElement element : driver.findElements(By.className("upm-plugin-name")))
        {
            if (element.getText().trim().equals(pluginKeyAndName))
            {
                element.click();
                By byConfigure = By.linkText("Configure");
                driver.waitUntilElementIsVisible(byConfigure);
                WebElement configureLink = driver.findElement(byConfigure);
                if (configureLink.getAttribute("href").endsWith(
                        pluginKeyAndName + "/" + pageKey))
                {
                    configureLink.click();
                    return pageBinder.bind(nextPage, pageKey, extraPrefix);
                }
            }
        }
        throw new IllegalStateException("Didn't find plugin");
    }
    
    public <P extends Object> P configurePlugin(String pluginKeyAndName, String pageKey, Class<P> nextPage)
    {
        return configurePlugin(pluginKeyAndName,pageKey,nextPage,"");
    }
}
