package com.atlassian.plugin.connect.test.pageobjects;

import org.openqa.selenium.By;

import java.util.concurrent.Callable;

public class RemotePluginEmbeddedTestPage extends RemotePage
{
    public RemotePluginEmbeddedTestPage(String pageKey)
    {
        super(pageKey);
    }

    public String getFullName()
    {
        return waitForValue("user");
    }

    public String getUserId()
    {
        return waitForValue("userId");
    }

    public String getTimeZone()
    {
        return waitForValue("timeZone");
    }

    public String getLocale()
    {
        return waitForValue("locale");
    }

    public String getMessage()
    {
        return getValue("message");
    }

    public String getConsumerKey()
    {
        return getValue("consumerKey");
    }

    public String getClientHttpStatus()
    {
        return waitForValue("client-http-status");
    }

    public String getClientHttpStatusText()
    {
        return waitForValue("client-http-status-text");
    }

    public String getClientHttpContentType()
    {
        return waitForValue("client-http-content-type");
    }

    public String getClientHttpResponseText()
    {
        return waitForValue("client-http-response-text");
    }

    public String getClientHttpData()
    {
        return waitForValue("client-http-data");
    }

    public String getClientHttpDataJson()
    {
        return waitForValue("client-http-data-json");
    }

    public String getClientHttpDataXml()
    {
        return waitForValue("client-http-data-xml");
    }

    public String getValueBySelector(final String selector)
    {
        return runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.cssSelector(selector)).getText();
            }
        });
    }
}
