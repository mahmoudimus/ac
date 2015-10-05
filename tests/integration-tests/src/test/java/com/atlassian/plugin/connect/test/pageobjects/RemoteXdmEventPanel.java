package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to emit client-side XDM events
 */
public class RemoteXdmEventPanel
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    private final String addOnId;
    private final String moduleId;

    protected WebElement containerDiv;

    public RemoteXdmEventPanel(String addOnId, String moduleId)
    {
        this.addOnId = addOnId;
        this.moduleId = moduleId;
    }

    @Init
    public void init()
    {
        By selector = By.id("embedded-" + ModuleKeyUtils.addonAndModuleKey(addOnId, moduleId));
        driver.waitUntilElementIsLocated(selector);
        this.containerDiv = driver.findElement(selector);
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(@Nullable WebDriver input)
            {
                return containerDiv.getAttribute("class").contains("iframe-init");
            }
        });
    }

    public void emit()
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("emit-button"));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
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
        return runInFrame(driver, containerDiv, new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                try
                {
                    driver.waitUntil(new Function<WebDriver, Boolean>()
                    {
                        @Override
                        public Boolean apply(WebDriver webDriver)
                        {
                            return webDriver.findElement(selector) != null;
                        }
                    }, 1);
                }
                catch (TimeoutException e)
                {
                    return true;
                }
                return false;
            }
        });
    }

    public String waitForValue(String key)
    {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }
}