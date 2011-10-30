package com.atlassian.labs.remoteapps.test;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.refapp.RefappTestedProduct;

public class OwnerOfTestedProduct
{
    public static final TestedProduct INSTANCE;

    static
    {
        INSTANCE = TestedProductFactory.create(System.getProperty("testedProductClass", RefappTestedProduct.class.getName()));
        /*if (INSTANCE instanceof JiraTestedProduct)
        {
            INSTANCE.getPageBinder().override(SpeakeasyUserPage.class, JiraSpeakeasyUserPage.class);
            INSTANCE.getPageBinder().override(UnauthorizedUserPage.class, JiraUnauthorizedUserPage.class);
        }*/
    }
}