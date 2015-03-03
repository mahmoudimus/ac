package it;

import com.atlassian.plugin.connect.test.BaseUrlLocator;

public class AbstractBrowserlessTest
{
    protected final String baseUrl;

    public AbstractBrowserlessTest()
    {
        baseUrl = BaseUrlLocator.getBaseUrl();
    }
}
