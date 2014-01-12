package it.confluence;

import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import it.ConnectWebDriverTestBase;
import org.junit.BeforeClass;

public abstract class ConfluenceWebDriverTestBase extends ConnectWebDriverTestBase
{
    static
    {
        System.setProperty("testedProductClass", FixedConfluenceTestedProduct.class.getName());
    }

    protected static ConfluenceOps confluenceOps;

    @BeforeClass
    public static void setUpConfluence()
    {
        confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());
    }

}
