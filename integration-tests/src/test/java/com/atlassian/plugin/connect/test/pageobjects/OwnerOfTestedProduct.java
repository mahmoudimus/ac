package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceGeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdminSummaryPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraGeneralPage;

import com.google.common.base.Supplier;

import static com.google.common.base.Suppliers.memoize;


public class OwnerOfTestedProduct
{
    public static final Supplier<TestedProduct> INSTANCE = memoize(new Supplier<TestedProduct>()
    {
        @Override
        public TestedProduct get()
        {
            final TestedProduct testedProduct = getTestedProduct();
            configurePageBinder(testedProduct);
            return testedProduct;
        }
    });

    private static void configurePageBinder(final TestedProduct testedProduct)
    {
        final PageBinder pageBinder = testedProduct.getPageBinder();
        if (testedProduct instanceof JiraTestedProduct)
        {
            pageBinder.override(AdminHomePage.class, JiraAdminSummaryPage.class);
            pageBinder.override(GeneralPage.class, JiraGeneralPage.class);
            pageBinder.override(HomePage.class, DashboardPage.class);
        }
        else if (testedProduct instanceof ConfluenceTestedProduct)
        {
            pageBinder.override(GeneralPage.class, ConfluenceGeneralPage.class);
        }
    }

    private static TestedProduct getTestedProduct()
    {
        if (null != System.getProperty("testedProduct"))
        {
            if ("jira".equalsIgnoreCase(System.getProperty("testedProduct")))
            {
                return TestedProductFactory.create(JiraTestedProduct.class.getName());
            }
            else if ("confluence".equalsIgnoreCase(System.getProperty("testedProduct")))
            {
                return TestedProductFactory.create(FixedConfluenceTestedProduct.class.getName());
            }
            else
            {
                return TestedProductFactory.create(System.getProperty("testedProductClass", JiraTestedProduct.class.getName()));
            }
        }
        else
        {
            return TestedProductFactory.create(System.getProperty("testedProductClass", JiraTestedProduct.class.getName()));
        }
    }
}
