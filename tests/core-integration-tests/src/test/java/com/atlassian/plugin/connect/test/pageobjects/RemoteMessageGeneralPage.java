package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Page with a single button to open a dialog
 */
public class RemoteMessageGeneralPage extends ConnectAddonPage implements Page {

    @Inject
    protected PageElementFinder elementFinder;

    public RemoteMessageGeneralPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl() {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }

    public void openInfoMessage() {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id("display-message"));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

    public String getMessageTitleText() {
        return elementFinder.find(By.cssSelector("#ac-message-container .aui-message .title")).getText();
    }
}
