package com.atlassian.plugin.connect.test.product;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.common.util.ConnectTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

/**
 * A simple SPI to be implemented by host-product-specific modules allowing
 * access to common functions such as: <ul> <li>The TestedProduct instance</li>
 * <li>User creation</li> <li>Login / logout</li> </ul>
 *
 * @since v1.1.58
 */
public interface TestedProductAccessor {
    void login(TestUser user);

    <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args);

    TestedProduct<WebDriverTester> getTestedProduct();

    ConnectTestUserFactory getUserFactory();

    String getGloballyVisibleLocation();

    static TestedProductAccessor get() {
        switch (System.getProperty("testedProduct", "")) {
            case "confluence":
                return new ConfluenceTestedProductAccessor();
            default:
                return new JiraTestedProductAccessor();
        }
    }
}
