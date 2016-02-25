package com.atlassian.plugin.connect.test.common.pageobjects;

import org.openqa.selenium.By;

public class ConnectAddonEmbeddedTestPage extends ConnectAddonPage {
    public ConnectAddonEmbeddedTestPage(String pageKey, boolean includeEmbeddedPrefix) {
        this("", pageKey, includeEmbeddedPrefix);
    }

    public ConnectAddonEmbeddedTestPage(String addonKey, String pageKey, boolean includeEmbeddedPrefix) {
        super(addonKey, pageKey, includeEmbeddedPrefix);
    }

    public String getFullName() {
        return waitForValue("user");
    }

    public String getUserId() {
        return waitForValue("userId");
    }

    public String getTimeZone() {
        return waitForValue("timeZone");
    }

    public String getLocale() {
        return waitForValue("locale");
    }

    public String getMessage() {
        return getValue("message");
    }

    public String getLocation() {
        return getValue("location");
    }

    public String getClientHttpStatus() {
        return waitForValue("client-http-status");
    }

    public String getClientHttpStatusText() {
        return waitForValue("client-http-status-text");
    }

    public String getClientHttpContentType() {
        return waitForValue("client-http-content-type");
    }

    public String getClientHttpResponseText() {
        return waitForValue("client-http-response-text");
    }

    public String getClientHttpData() {
        return waitForValue("client-http-data");
    }

    public String getClientHttpDataJson() {
        return waitForValue("client-http-data-json");
    }

    public String getClientHttpDataXml() {
        return waitForValue("client-http-data-xml");
    }

    public String getValueBySelector(final String selector) {
        return runInFrame(() -> driver.findElement(By.cssSelector(selector)).getText());
    }

    public String getValueById(final String id) {
        return runInFrame(() -> driver.findElement(By.id(id)).getText());
    }

    public String getTitle() {
        return driver.getTitle();
    }
}
