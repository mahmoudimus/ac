package com.atlassian.labs.remoteapps.test;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceGeneralPage;
import com.atlassian.labs.remoteapps.test.jira.JiraGeneralPage;
import com.atlassian.labs.remoteapps.test.refapp.RefappFixedLoginPage;
import com.atlassian.labs.remoteapps.test.refapp.RefappGeneralPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.confluence.ConfluenceTestedProduct;
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
        }
        else if (INSTANCE instanceof RefappTestedProduct)
        {
            INSTANCE.getPageBinder().override(LoginPage.class, RefappFixedLoginPage.class);
            INSTANCE.getPageBinder().override(GeneralPage.class, RefappGeneralPage.class);
        }
    }
}