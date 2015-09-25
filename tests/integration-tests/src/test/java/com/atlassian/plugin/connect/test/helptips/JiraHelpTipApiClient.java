package com.atlassian.plugin.connect.test.helptips;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import it.util.TestUser;

/**
 * A client for the JIRA Help Tip REST API.
 */
public class JiraHelpTipApiClient extends HelpTipApiClient
{

    public JiraHelpTipApiClient(JiraTestedProduct product, TestUser user)
    {
        super(product, user);
    }

    public void dismissAllHelpTips() throws Exception
    {
        dismissBrowseProjectHelpTips();
        dismissConfigureProjectTips();
        dismissMiscellaneousHelpTips();
    }

    public void dismissBrowseProjectHelpTips() throws Exception
    {
        dismissHelpTip("sidebar-chaperone-collapse-tip");
        dismissHelpTip("sidebar-chaperone-disable-tip");
        dismissHelpTip("sidebar-chaperone-general-overview-tip");
    }

    public void dismissConfigureProjectTips() throws Exception
    {
        dismissHelpTip("hipchat.feature.discovery.tip");
    }

    public void dismissMiscellaneousHelpTips() throws Exception
    {
        dismissHelpTip("automaticTransitionDevSummaryTooltip");
        dismissHelpTip("devstatus.cta.createbranch.tooltip");
        dismissHelpTip("permission-helper-helptip");
        dismissHelpTip("split-view-intro");
        dismissHelpTip("view.all.issues");
    }
}
