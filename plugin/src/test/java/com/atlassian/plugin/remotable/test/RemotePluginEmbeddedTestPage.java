package com.atlassian.plugin.remotable.test;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.plugin.remotable.pageobjects.RemotePage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
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

	public String getTimeZoneFromTemplateContext()
	{
		return getValue("timeZoneFromTemplateContext");
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

    public String getServerHttpStatus()
    {
        return getValue("server-http-status");
    }

    public String getServerHttpStatusText()
    {
        return getValue("server-http-status-text");
    }

    public String getServerHttpContentType()
    {
        return getValue("server-http-content-type");
    }

    public String getServerHttpEntity()
    {
        return getValue("server-http-entity");
    }

    String getValue(final String key)
    {
        return runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.id(key)).getText();
            }
        });
    }

    String waitForValue(final String key)
    {
        runInFrame(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                driver.waitUntil(new Function<WebDriver, Boolean>() {

                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        WebElement element = webDriver.findElement(By.id(key));
                        return element.getText() != null;
                    }
                });
                return null;
            }
        });

        return getValue(key);
    }
}
