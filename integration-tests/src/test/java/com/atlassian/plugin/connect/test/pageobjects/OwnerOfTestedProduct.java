package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceAdminHomePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceDashboardPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceLoginPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdminSummaryPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;

public class OwnerOfTestedProduct
{
    public static final TestedProduct INSTANCE;

    static
    {
        INSTANCE = TestedProductFactory.create(System.getProperty("testedProductClass", JiraTestedProduct.class.getName()));
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
    }
}