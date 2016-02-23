package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class PlainTextView {
    @Inject
    AtlassianWebDriver driver;

    @WaitUntil
    public void waitForRedirect() {
        // this is how we detect is is a plain text page
        driver.waitUntilElementIsNotLocated(By.tagName("div"));
    }

    public String getContent() {
        // for some reason, the driver always thinks it is html, so getPageSource() returns some
        // xhtml with a <pre> body.  This may be firefox specific...
        return driver.findElement(By.tagName("pre")).getText();
    }
}
