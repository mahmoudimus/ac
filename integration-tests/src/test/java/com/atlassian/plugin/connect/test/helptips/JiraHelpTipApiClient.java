package com.atlassian.plugin.connect.test.helptips;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.client.UserRequestSender;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import it.util.TestUser;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONObject;

import java.io.IOException;

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
