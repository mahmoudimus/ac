package com.atlassian.connect.test.jira.pageobjects;

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
        driver.findElements(By.className("aui-list-item-link")).stream()
            .filter(element -> name.equals(element.getText()))
            .forEach(org.openqa.selenium.WebElement::click);
        return pageBinder.bind(viewClass);
    }
}
