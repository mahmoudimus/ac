package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;

public class CreatedIssueMessage
{
    @Inject
    AtlassianWebDriver driver;

    @WaitUntil
    public void waitForMessage()
    {
        driver.waitUntilElementIsVisible(By.className("issue-created-key"));
    }

    public String getKey()
    {
        String href = driver.findElement(By.className("issue-created-key")).getAttribute("href");
        return href.substring(href.lastIndexOf('/') + 1);
    }


}
