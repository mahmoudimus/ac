package it;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.BaseUrlLocator;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

public class AbstractBrowserlessTest
{
    protected final String baseUrl;
    protected final static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;

    public AbstractBrowserlessTest()
    {
        this(product.getClass());
    }

    private static Class findClass(String name)
    {
        try
        {
            return Class.forName(name);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public AbstractBrowserlessTest(Class<? extends TestedProduct> testedProductClass)
    {
        baseUrl = BaseUrlLocator.getBaseUrl(testedProductClass);
    }
}
