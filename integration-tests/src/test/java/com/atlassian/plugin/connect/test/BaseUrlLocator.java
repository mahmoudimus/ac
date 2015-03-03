package com.atlassian.plugin.connect.test;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

public class BaseUrlLocator
{
    protected final static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE.get();
    
    public static String getBaseUrl()
    {
        return OwnerOfTestedProduct.INSTANCE.get().getProductInstance().getBaseUrl();
    }
}
