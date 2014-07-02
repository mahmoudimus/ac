package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Helper methods for retrieving the content from an iframe.
 */
public class RemotePageUtil
{
    private static final Logger log = LoggerFactory.getLogger(RemotePageUtil.class);
    private static final String IFRAME = "iframe";

    public static <T> T runInFrame(AtlassianWebDriver driver, WebElement containerDiv, Callable<T> callable)
    {
        toIframe(driver, containerDiv);
        T result = null;
        try
        {
            result = callable.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Nested operation in iframe failed", e);
        }
        outIframe(driver);
        return result;
    }

    public static String waitForValue(final AtlassianWebDriver driver, final WebElement containerDiv, final String key)
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                driver.waitUntil(new Function<WebDriver, Boolean>()
                {

                    @Override
                    public Boolean apply(WebDriver webDriver)
                    {
                        WebElement element = webDriver.findElement(By.id(key));
                        return element.getText() != null;
                    }
                });
                return null;
            }
        });

        return getValue(driver, containerDiv, key);
    }

    public static String getValue(final AtlassianWebDriver driver, final WebElement containerDiv, final String key)
    {
        return runInFrame(driver, containerDiv, new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.id(key)).getText();
            }
        });
    }

    public static void toIframe(AtlassianWebDriver driver, WebElement containerDiv)
    {
        WebElement iFrame = null;

        try
        {
            iFrame = containerDiv.findElement(By.tagName(IFRAME));
        }
        catch (NoSuchElementException e)
        {
            log.error("Failed to find a <{}> inside a <{} id=\"{}\">", new String[]{IFRAME, containerDiv.getTagName(), containerDiv.getAttribute("id")});
            throw e;
        }

        driver.getDriver().switchTo().frame(iFrame);
    }

    public static void outIframe(AtlassianWebDriver driver)
    {
        driver.getDriver().switchTo().defaultContent();
    }

    public static Map<String, String> findAllInContext(final String src)
    {
        return findInContextHelper(src, FIND_ALL_IN_CONTEXT_FUNCTION);
    }

    public static String findInContext(final String src, final String key)
    {
        return findInContextHelper(src, new FindInContextFunction(key));
    }

    public static <T> T findInContextHelper(final String src, final Function<List<NameValuePair>, T> function)
    {
        return function.apply(URLEncodedUtils.parse(URI.create(src), "UTF-8"));
    }

    private final static Function<List<NameValuePair>, Map<String, String>> FIND_ALL_IN_CONTEXT_FUNCTION = new Function<List<NameValuePair>, Map<String, String>>()
    {
        @Override
        public Map<String, String> apply(final List<NameValuePair> input)
        {
            final Map<String, String> result = newHashMap();
            for (final NameValuePair pair : input)
            {
                result.put(pair.getName(), pair.getValue());
            }
            return result;
        }
    };

    private static class FindInContextFunction implements Function<List<NameValuePair>, String> {
        private final String key;

        private FindInContextFunction(final String key)
        {
            this.key = key;
        }
        @Override
        @Nullable
        public String apply(final List<NameValuePair> input)
        {
            for (final NameValuePair pair : input)
            {
                if (key.equals(pair.getName()))
                {
                    return pair.getValue();
                }
            }
            return null;
        }
    }

}
