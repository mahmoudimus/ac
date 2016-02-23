package com.atlassian.plugin.connect.test.common.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class ConnectAddonHelloWorldPage {
    @Inject
    private PageElementFinder elementFinder;

    @WaitUntil
    public void waitForInit() {
        waitForFirstScriptToLoad();
    }

    private void waitForFirstScriptToLoad() {
        PageElement element = elementFinder.find(By.tagName("script"));
        waitUntilTrue(element.timed().isPresent());
    }
}
