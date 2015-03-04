package com.atlassian.plugin.connect.test;

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
 * A client for the Help Tip REST API.
 */
public class JiraHelpTipApiClient
{

    private final String baseUrl;
    private final String defaultUsername;
    private final String defaultPassword;
    private final UserRequestSender userRequestSender;

    public JiraHelpTipApiClient(JiraTestedProduct product, TestUser user)
    {
        this.baseUrl = product.getProductInstance().getBaseUrl();
        this.defaultUsername = user.getUsername();
        this.defaultPassword = user.getPassword();
        this.userRequestSender = new UserRequestSender(baseUrl);
    }

    public void dismissProjectAdminHipChatHelpTip() throws Exception
    {
        dismissHelpTip("hipchat.feature.discovery.tip");
    }

    private void dismissHelpTip(String tipId) throws Exception
    {
        HttpPost request = new HttpPost(baseUrl + "/rest/helptips/1.0/tips");
        request.setEntity(new StringEntity(new JSONObject(ImmutableMap.<String, Object>of("id", tipId)).toString(), ContentType.APPLICATION_JSON));
        userRequestSender.sendRequestAsUser(request, new BasicResponseHandler(), defaultUsername, defaultPassword);
    }
}
