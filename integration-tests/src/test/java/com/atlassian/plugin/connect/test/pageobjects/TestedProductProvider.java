package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdminSummaryPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;
import com.atlassian.util.concurrent.ResettableLazyReference;

public class TestedProductProvider
{
    public static JiraTestedProduct getJiraTestedProduct()
    {
        return jiraTestedProduct.get();
    }

    public static ConfluenceTestedProduct getConfluenceTestedProduct()
    {
        return confluenceTestedProduct.get();
    }

    public static TestedProduct getTestedProduct()
    {
        switch (System.getProperty("testedProduct", ""))
        {
            case "jira":
                return getJiraTestedProduct();
            case "confluence":
                return getConfluenceTestedProduct();
            default:
                return testedProduct.get();
        }
    }

    private static ResettableLazyReference<TestedProduct<?>> testedProduct = new ResettableLazyReference<TestedProduct<?>>()
    {
        @Override
        protected TestedProduct<?> create() throws Exception
        {
            return TestedProductFactory.create(System.getProperty("testedProductClass", JiraTestedProduct.class.getName()));
        }
    };


    private static ResettableLazyReference<ConfluenceTestedProduct> confluenceTestedProduct = new ResettableLazyReference<ConfluenceTestedProduct>()
    {
        @Override
        protected ConfluenceTestedProduct create() throws Exception
        {
            FixedConfluenceTestedProduct testedProduct = TestedProductFactory.create(FixedConfluenceTestedProduct.class);
            testedProduct.getPageBinder().override(GeneralPage.class, ConfluenceGeneralPage.class);
            return testedProduct;
        }
    };

    private static ResettableLazyReference<JiraTestedProduct> jiraTestedProduct = new ResettableLazyReference<JiraTestedProduct>()
    {
        @Override
        protected JiraTestedProduct create() throws Exception
        {
            JiraTestedProduct testedProduct = TestedProductFactory.create(JiraTestedProduct.class);
            testedProduct.getPageBinder().override(AdminHomePage.class, JiraAdminSummaryPage.class);
            testedProduct.getPageBinder().override(GeneralPage.class, JiraGeneralPage.class);
            testedProduct.getPageBinder().override(HomePage.class, DashboardPage.class);
            return testedProduct;
        }
    };
}
