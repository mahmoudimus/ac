package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 15/03/12 Time: 1:38 PM To change this template use
 * File | Settings | File Templates.
 */
@Defaults(instanceId = "confluence", contextPath = "/confluence", httpPort = 1990)
public class FixedConfluenceTestedProduct extends ConfluenceTestedProduct
{
    public FixedConfluenceTestedProduct(
            TestedProductFactory.TesterFactory<WebDriverTester> testerFactory,
            ProductInstance productInstance)
    {
        super(testerFactory, productInstance);
    }
}
