package com.atlassian.plugin.connect.test.client;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * A helper class for downloading data from a URL, using the cookies from the current WebDriver session.
 */
public class WebDriverSessionAwareDownloader
{
    private final WebDriver driver;

    public WebDriverSessionAwareDownloader(WebDriver driver)
    {
        this.driver = driver;
    }

    public byte[] downloadBytes(String url) throws IOException
    {
        CloseableHttpClient httpClient = getHttpClient();
        try
        {
            HttpUriRequest request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);
            InputStream content = response.getEntity().getContent();
            try
            {
                return IOUtils.toByteArray(content);
            }
            finally
            {
                content.close();
            }
        }
        finally
        {
            httpClient.close();
        }
    }

    private CloseableHttpClient getHttpClient()
    {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultCookieStore(getWebdriverCookies());
        return builder.build();
    }

    private CookieStore getWebdriverCookies()
    {
        CookieStore cookieStore = new BasicCookieStore();
        Set<Cookie> webdriverCookies = driver.manage().getCookies();
        for (Cookie webdriverCookie : webdriverCookies)
        {
            BasicClientCookie cookie = new BasicClientCookie(webdriverCookie.getName(), webdriverCookie.getValue());
            cookie.setDomain(webdriverCookie.getDomain());
            cookie.setPath(webdriverCookie.getPath());
            cookie.setExpiryDate(webdriverCookie.getExpiry());
            cookie.setSecure(webdriverCookie.isSecure());
            cookieStore.addCookie(cookie);
        }
        return cookieStore;
    }
}
