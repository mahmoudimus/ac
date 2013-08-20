package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ViewChangingSearchResult
{

    @Inject
    AtlassianWebDriver driver;
    @Inject
    PageBinder pageBinder;

    public <T> T openView(String name, Class<T> viewClass)
    {
        driver.findElement(By.id("viewOptions")).click();
        for (WebElement element : driver.findElements(By.className("aui-list-item-link")))
        {
            if (name.equals(element.getText()))
            {
                element.click();
            }
        }
        return pageBinder.bind(viewClass);
    }
}
