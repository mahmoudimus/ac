package com.atlassian.plugin.connect.test.confluence.util;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.common.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.common.util.TestUser;

/**
 * A client for the Confluence Help Tip REST API.
 */
public class ConfluenceHelpTipApiClient extends HelpTipApiClient {

    public ConfluenceHelpTipApiClient(ConfluenceTestedProduct product, TestUser user) {
        super(product, user);
    }

    public void dismissAllHelpTips() throws Exception {
        dismissHelpTip("cq.feature.discovery.share");
        dismissHelpTip("cq.space.sidebar.discovery");
    }
}
