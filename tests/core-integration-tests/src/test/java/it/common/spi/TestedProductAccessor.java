package it.common.spi;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import it.util.ConnectTestUserFactory;
import it.util.TestUser;

/**
 * A simple SPI to be implemented by host-product-specific modules allowing
 * access to common functions such as:
 * <ul>
 *     <li>The TestedProduct instance</li>
 *     <li>User creation</li>
 *     <li>Login / logout</li>
 * </ul>
 *
 * @since v1.1.58
 */
public interface TestedProductAccessor
{
    void login(TestUser user);

    <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args);

    TestedProduct<WebDriverTester> getTestedProduct();

    ConnectTestUserFactory getUserFactory();

    String getGloballyVisibleLocation();

    static TestedProductAccessor get()
    {
        try
        {
            return (TestedProductAccessor) Class.forName("com.atlassian.connect.test.confluence.pageobjects.ConfluenceTestedProductAccessor").getConstructor().newInstance();
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException("Could not retrieve a TestedProductAccessor!");
        }
    }
}
