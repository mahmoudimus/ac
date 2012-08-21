package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

import static com.atlassian.labs.remoteapps.plugin.installer.RemoteAppDescriptorPluginArtifactFactory.calculatePluginName;

public class PluginManagerPage implements Page
{
    @Inject
    AtlassianWebDriver driver;

    @Inject
    PageBinder pageBinder;

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/upm";
    }

    @WaitUntil
    public void waitForLoading()
    {
        driver.waitUntilElementIsLocated(By.id("upm-current-plugins"));
        WebElement userPlugins = driver.findElement(By.id("upm-current-plugins"));
        driver.waitUntilElementIsNotLocatedAt(By.className("loading"), userPlugins);
    }

    public <P extends Object> P configurePlugin(String pluginKeyAndName, String pageKey, Class<P> nextPage)
    {
        for (WebElement element : driver.findElements(By.className("upm-plugin-name")))
        {
            if (element.getText().trim().equals(calculatePluginName(pluginKeyAndName)))
            {
                element.click();
                By byConfigure = By.linkText("Configure");
                driver.waitUntilElementIsVisible(byConfigure);
                WebElement configureLink = driver.findElement(byConfigure);
                if (configureLink.getAttribute("href").endsWith(
                        pluginKeyAndName + "/" + pageKey))
                {
                    configureLink.click();
                    return pageBinder.bind(nextPage, pageKey);
                }
            }
        }
        throw new IllegalStateException("Didn't find plugin");
    }
}
