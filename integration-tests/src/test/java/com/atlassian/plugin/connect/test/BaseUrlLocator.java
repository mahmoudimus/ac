package com.atlassian.plugin.connect.test;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

public class BaseUrlLocator
{
    protected final static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;

    public static String getBaseUrl()
    {
        return getBaseUrl(product.getClass());
    }

    public static String getBaseUrl(Class<? extends TestedProduct> testedProductClass)
    {
        String baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();

        return baseUrl;
    }
}
