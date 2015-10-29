package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.confluence.it.User;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import it.common.spi.TestedProductAccessor;
import it.confluence.util.ConfluenceTestUserFactory;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;

public class ConfluenceTestedProductAccessor implements TestedProductAccessor
{
    private final FixedConfluenceTestedProduct product;

    public ConfluenceTestedProductAccessor()
    {
        product = getConfluenceProduct();
    }

    @Override
    public void login(TestUser user)
    {
        product.visit(LoginPage.class).login(user.getUsername(), user.getPassword(), HomePage.class);
    }

    @Override
    public <P extends Page> P loginAndVisit(TestUser user, Class<P> page, Object... args)
    {
        return product.login(toConfluenceUser(user), page, args);
    }

    @Override
    public TestedProduct<WebDriverTester> getTestedProduct()
    {
        return getConfluenceProduct();
    }

    public static User toConfluenceUser(TestUser user)
    {
        return new User(user.getUsername(), user.getPassword(), user.getDisplayName(), user.getEmail());
    }

    @Override
    public ConnectTestUserFactory getUserFactory()
    {
        return new ConfluenceTestUserFactory(product);
    }

    @Override
    public String getGloballyVisibleLocation()
    {
        return "system.header/left";
    }

    public FixedConfluenceTestedProduct getConfluenceProduct()
    {
        FixedConfluenceTestedProduct product;
        product = TestedProductFactory.create(FixedConfluenceTestedProduct.class);
        product.getPageBinder().override(GeneralPage.class, ConfluenceGeneralPage.class);
        return product;
    }
}
