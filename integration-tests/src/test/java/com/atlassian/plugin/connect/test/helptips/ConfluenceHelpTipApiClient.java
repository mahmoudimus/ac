package com.atlassian.plugin.connect.test.helptips;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
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
