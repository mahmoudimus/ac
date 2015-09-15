package com.atlassian.plugin.connect.test.helptips;

import com.atlassian.confluence.webdriver.pageobjects.ConfluenceTestedProduct;
import it.util.TestUser;

/**
 * A client for the Confluence Help Tip REST API.
 */
public class ConfluenceHelpTipApiClient extends HelpTipApiClient
{

    public ConfluenceHelpTipApiClient(ConfluenceTestedProduct product, TestUser user)
    {
        super(product, user);
    }

    public void dismissAllHelpTips() throws Exception
    {
        dismissHelpTip("cq.feature.discovery.share");
        dismissHelpTip("cq.space.sidebar.discovery");
    }
}
