package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

@Defaults(instanceId = "confluence", contextPath = "/confluence", httpPort = 8080)
public class FixedConfluenceTestedProduct extends ConfluenceTestedProduct
{
    public FixedConfluenceTestedProduct(TestedProductFactory.TesterFactory<WebDriverTester> testerFactory, ProductInstance productInstance)
    {
        super(testerFactory, productInstance);
    }
}
