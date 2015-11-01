package com.atlassian.plugin.connect.test.pageobjects;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Page with a single button to open a dialog
 */
public class RemoteMessageGeneralPage extends ConnectAddOnPage implements Page
{

    @Inject
    protected PageElementFinder elementFinder;

    public RemoteMessageGeneralPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addOnKey, pageElementKey);
    }

    public void openInfoMessage()
    {
        runInFrame(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("display-message"));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
        });
    }

    public String getMessageTitleText()
    {
        return elementFinder.find(By.cssSelector("#ac-message-container .aui-message .title")).getText();
    }
}