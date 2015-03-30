package com.atlassian.plugin.connect.test;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;

public class BaseUrlLocator
{
    protected final static TestedProduct product = TestedProductProvider.getTestedProduct();

    public static String getBaseUrl()
    {
        return product.getProductInstance().getBaseUrl();
    }
}
