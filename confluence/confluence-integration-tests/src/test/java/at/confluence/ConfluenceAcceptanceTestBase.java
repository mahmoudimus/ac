package at.confluence;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;

import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;

public class ConfluenceAcceptanceTestBase {
    protected static final TestUser ADMIN = new TestUser("admin");
    protected final ConfluenceTestedProduct product = new ConfluenceTestedProductAccessor().getConfluenceProduct();

    protected DashboardPage login(TestUser user) {
        return login(user, DashboardPage.class);
    }

    protected <T extends Page> T login(TestUser user, Class<T> page, Object... args) {
        return product.login(toConfluenceUser(user), page, args);
    }
}
