package com.atlassian.plugin.connect.test.helptips;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProduct;

import it.util.TestUser;

public class HelpTipApiClientFactory
{
    public static HelpTipApiClient getHelpTipApiClient(TestedProduct product, TestUser user)
    {
        if (product instanceof JiraTestedProduct)
        {
            return new JiraHelpTipApiClient( (JiraTestedProduct) product, user);
        }
        return new ConfluenceHelpTipApiClient( (ConfluenceTestedProduct) product, user);
    }
}
