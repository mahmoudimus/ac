package com.atlassian.plugin.connect.test.jira.product;

import com.atlassian.plugin.connect.test.jira.pageobjects.JiraAdminSummaryPage;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.jira.pageobjects.JiraGeneralPage;
import com.atlassian.plugin.connect.test.product.TestedProductAccessor;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import com.atlassian.plugin.connect.test.jira.util.JiraTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.ConnectTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.TestUser;

public class JiraTestedProductAccessor implements TestedProductAccessor
{
    private final JiraTestedProduct product;

    public JiraTestedProductAccessor()
    {
        product = getJiraProduct();
    }

    @Override
    public void login(TestUser user)
    {
        product.quickLogin(user.getUsername(), user.getPassword());
    }

    @Override
    public <P extends Page> P loginAndVisit(TestUser user, Class<P> page, Object... args)
    {
        return product.quickLogin(user.getUsername(), user.getPassword(), page, args);
    }

    @Override
    public TestedProduct<WebDriverTester> getTestedProduct()
    {
        return getJiraProduct();
    }

    @Override
    public ConnectTestUserFactory getUserFactory()
    {
        return new JiraTestUserFactory(product);
    }

    @Override
    public String getGloballyVisibleLocation()
    {
        return "system.top.navigation.bar";
    }

    public JiraTestedProduct getJiraProduct()
    {
        JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);
        product.backdoor().darkFeatures().enableForSite("jira.onboarding.feature.disabled");

        product.getPageBinder().override(AdminHomePage.class, JiraAdminSummaryPage.class);
        product.getPageBinder().override(GeneralPage.class, JiraGeneralPage.class);
        product.getPageBinder().override(HomePage.class, DashboardPage.class);
        return product;
    }
}
