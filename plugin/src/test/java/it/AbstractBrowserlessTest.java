package it;

import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.refapp.RefappTestedProduct;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.MethodRule;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 25/04/12 Time: 3:54 PM To change this template use
 * File | Settings | File Templates.
 */
public class AbstractBrowserlessTest
{
    protected static final String baseUrl;

    static
    {
        if (System.getProperty("baseurl") == null)
        {
            try
            {
                Class cls = Class.forName(System.getProperty("testedProductClass",
                        RefappTestedProduct.class.getName()));
                Defaults defs = (Defaults) cls.getAnnotation(Defaults.class);
                baseUrl = "http://localhost:" + defs.httpPort() + defs.contextPath();
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();
        }

    }
}
