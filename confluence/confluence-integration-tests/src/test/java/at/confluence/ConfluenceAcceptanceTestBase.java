package at.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;

import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;

public class ConfluenceAcceptanceTestBase
{
    protected static final TestUser ADMIN = new TestUser("admin");
    protected final ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();

    protected void login(TestUser user)
    {
        product.login(toConfluenceUser(user), DashboardPage.class);
    }
}
