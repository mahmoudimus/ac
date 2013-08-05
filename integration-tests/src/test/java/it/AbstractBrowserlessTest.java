package it;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;

public class AbstractBrowserlessTest
{
    protected final String baseUrl;

    public AbstractBrowserlessTest()
    {
        this((Class<? extends TestedProduct>) findClass(System.getProperty("testedProductClass", JiraTestedProduct.class.getName())));
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
