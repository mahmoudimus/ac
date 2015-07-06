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

public class TestedProductProvider
{
    public static JiraTestedProduct getJiraTestedProduct()
    {
        JiraTestedProduct testedProduct = TestedProductFactory.create(JiraTestedProduct.class);
        customizeJiraTestedProduct(testedProduct);
        return testedProduct;
    }

    public static ConfluenceTestedProduct getConfluenceTestedProduct()
    {
        FixedConfluenceTestedProduct testedProduct = TestedProductFactory.create(FixedConfluenceTestedProduct.class);
        customizeConfluenceTestedProduct(testedProduct);
        return testedProduct;
    }

    public static TestedProduct getTestedProduct()
    {
        String testedProductClassName = getTestedProductClassNameFromEnvironment();
        TestedProduct testedProduct = TestedProductFactory.create(testedProductClassName);
        return customizeTestedProduct(testedProduct);
    }

    private static String getTestedProductClassNameFromEnvironment()
    {
        switch (System.getProperty("testedProduct", ""))
        {
            case "jira":
                return JiraTestedProduct.class.getName();
            case "confluence":
                return FixedConfluenceTestedProduct.class.getName();
            default:
                return System.getProperty("testedProductClass", JiraTestedProduct.class.getName());
        }
    }

    private static <T extends TestedProduct> T customizeTestedProduct(T testedProduct)
    {
        if (testedProduct instanceof JiraTestedProduct)
        {
            customizeJiraTestedProduct(testedProduct);
        }
        else if (testedProduct instanceof ConfluenceTestedProduct)
        {
            customizeConfluenceTestedProduct(testedProduct);
        }
        return testedProduct;
    }

    private static void customizeJiraTestedProduct(TestedProduct testedProduct)
    {
        testedProduct.getPageBinder().override(GeneralPage.class, ConfluenceGeneralPage.class);
    }

    private static void customizeConfluenceTestedProduct(TestedProduct testedProduct)
    {
        testedProduct.getPageBinder().override(AdminHomePage.class, JiraAdminSummaryPage.class);
        testedProduct.getPageBinder().override(GeneralPage.class, JiraGeneralPage.class);
        testedProduct.getPageBinder().override(HomePage.class, DashboardPage.class);
    }
}
