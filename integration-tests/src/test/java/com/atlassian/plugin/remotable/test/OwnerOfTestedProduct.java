package com.atlassian.plugin.remotable.test;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.remotable.test.confluence.FixedConfluenceAdminHomePage;
import com.atlassian.plugin.remotable.test.confluence.FixedConfluenceDashboardPage;
import com.atlassian.plugin.remotable.test.confluence.FixedConfluenceLoginPage;
import com.atlassian.plugin.remotable.test.jira.JiraGeneralPage;
import com.atlassian.plugin.remotable.test.refapp.RefappFixedLoginPage;
import com.atlassian.plugin.remotable.test.refapp.RefappGeneralPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.webdriver.refapp.RefappTestedProduct;

public class OwnerOfTestedProduct
{
    public static final TestedProduct INSTANCE;

    static
    {
        INSTANCE = TestedProductFactory.create(System.getProperty("testedProductClass", RefappTestedProduct.class.getName()));
        if (INSTANCE instanceof JiraTestedProduct)
        {
            INSTANCE.getPageBinder().override(AdminHomePage.class, JiraAdminSummaryPage.class);
            INSTANCE.getPageBinder().override(GeneralPage.class, JiraGeneralPage.class);
        }
        else if (INSTANCE instanceof ConfluenceTestedProduct)
        {
            INSTANCE.getPageBinder().override(GeneralPage.class, ConfluenceGeneralPage.class);
            INSTANCE.getPageBinder().override(LoginPage.class, FixedConfluenceLoginPage.class);
            INSTANCE.getPageBinder().override(AdminHomePage.class, FixedConfluenceAdminHomePage.class);
            INSTANCE.getPageBinder().override(HomePage.class, FixedConfluenceDashboardPage.class);

        }
        else if (INSTANCE instanceof RefappTestedProduct)
        {
            INSTANCE.getPageBinder().override(LoginPage.class, RefappFixedLoginPage.class);
            INSTANCE.getPageBinder().override(GeneralPage.class, RefappGeneralPage.class);
        }
    }
}