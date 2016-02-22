package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class RemoteNavigatorGeneralPage extends ConnectAddonPage implements Page {
    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    public RemoteNavigatorGeneralPage(String addOnKey, String moduleKey) {
        super(addOnKey, moduleKey, true);
    }

    @Override
    public String getUrl() {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }

    public String getMessage(final String id) {
        return runInFrame(() -> {
            PageElement element = elementFinder.find(By.id(id));
            waitUntilTrue(element.timed().isVisible());
            return element.getText();
        });
    }

}
