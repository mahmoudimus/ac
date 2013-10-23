package com.atlassian.plugin.connect.test;

import com.atlassian.pageobjects.Defaults;
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

//        if (System.getProperty("baseurl") == null)
//        {
//            Defaults defs = testedProductClass.getAnnotation(Defaults.class);
//
//            String host = System.getProperty("testedProductHost","localhost");
//
//            baseUrl = "http://" + host + ":" + defs.httpPort() + defs.contextPath();
//        }

        return baseUrl;
    }
}
