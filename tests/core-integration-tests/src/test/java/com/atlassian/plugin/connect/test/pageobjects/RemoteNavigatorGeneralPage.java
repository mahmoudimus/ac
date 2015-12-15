package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class RemoteNavigatorGeneralPage extends ConnectAddOnPage implements Page
{
    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    public RemoteNavigatorGeneralPage(String addOnKey, String moduleKey)
    {
        super(addOnKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addOnKey, pageElementKey);
    }

    public ConnectGeneralTestPage clickToNavigate(String id) {
        open(id);
        return pageBinder.bind(ConnectGeneralTestPage.class);
    }

    public void open(final String id)
    {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id(id));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

}
