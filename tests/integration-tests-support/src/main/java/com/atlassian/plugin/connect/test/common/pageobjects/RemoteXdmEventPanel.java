package com.atlassian.plugin.connect.test.common.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.common.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to emit client-side XDM events
 */
public class RemoteXdmEventPanel
{

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    private final String addonId;
    private final String moduleId;

    protected WebElement containerDiv;

    public RemoteXdmEventPanel(String addonId, String moduleId)
    {
        this.addonId = addonId;
        this.moduleId = moduleId;
    }

    @Init
    public void init()
    {
        By selector = By.id("embedded-" + ModuleKeyUtils.addonAndModuleKey(addonId, moduleId));
        driver.waitUntilElementIsLocated(selector);
        this.containerDiv = driver.findElement(selector);
        driver.waitUntil(input -> containerDiv.getAttribute("class").contains("iframe-init"));
    }

    public void emit()
    {
        runInFrame(driver, containerDiv, () -> {
            PageElement element = elementFinder.find(By.id("emit-button"));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

    public String getModuleId()
    {
        return waitForValue("panel-id");
    }

    public boolean hasLoggedEvent(String panelId, String eventId)
    {
        String logLineId = panelId + "-" + eventId;
        return waitForValue(logLineId).equals(logLineId);
    }

    public boolean hasNotLoggedEvent(String panelId, String eventId)
    {
        final String logLineId = panelId + "-" + eventId;
        final By selector = By.id(logLineId);
        return runInFrame(driver, containerDiv, () -> {
            try
            {
                driver.waitUntil(webDriver -> webDriver.findElement(selector) != null, 1);
            }
            catch (TimeoutException e)
            {
                return true;
            }
            return false;
        });
    }

    public String waitForValue(String key)
    {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }
}
