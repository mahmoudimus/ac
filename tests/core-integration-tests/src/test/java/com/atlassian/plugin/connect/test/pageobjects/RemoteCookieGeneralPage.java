package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Page with a single button to open a dialog
 */
public class RemoteCookieGeneralPage extends ConnectGeneralTestPage {

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    public RemoteCookieGeneralPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey);
    }

    public void saveCookie() {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id("save-cookie"));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

    public void readCookie() {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id("read-cookie"));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

    public void eraseCookie() {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id("erase-cookie"));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

    public String getCookieContents() {
        return getValueById("cookie-contents");
    }
}
