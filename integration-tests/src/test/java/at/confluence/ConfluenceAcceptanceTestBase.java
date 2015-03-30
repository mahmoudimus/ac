package at.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;

import at.util.ExternalAddonInstaller;
import it.util.TestUser;

public class ConfluenceAcceptanceTestBase
{
    protected static final FixedConfluenceTestedProduct product = TestedProductFactory.create(FixedConfluenceTestedProduct.class);
    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());
    protected static final ExternalAddonInstaller externalAddonInstaller =
            new ExternalAddonInstaller(product.getProductInstance().getBaseUrl(), TestUser.ADMIN);


    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        product.getTester().getDriver().manage().deleteAllCookies();
        return product.login(user.confUser(), page, args);
    }
}
