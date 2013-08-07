package it;

import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.refapp.RefappTestedProduct;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 25/04/12 Time: 3:54 PM To change this template use
 * File | Settings | File Templates.
 */
public class AbstractBrowserlessTest
{
    protected final String baseUrl;

    public AbstractBrowserlessTest()
    {
        this((Class<? extends TestedProduct>) findClass(System.getProperty("testedProductClass",
                RefappTestedProduct.class.getName())));
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
        if (System.getProperty("baseurl") == null)
        {
            Defaults defs = testedProductClass.getAnnotation(Defaults.class);
            baseUrl = "http://localhost:" + defs.httpPort() + defs.contextPath();
        }
        else
        {
            baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();
        }
    }
}
