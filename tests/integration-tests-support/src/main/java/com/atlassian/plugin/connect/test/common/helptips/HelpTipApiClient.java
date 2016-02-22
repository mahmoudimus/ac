package com.atlassian.plugin.connect.test.common.helptips;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.common.client.UserRequestSender;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import com.google.common.collect.ImmutableMap;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONObject;

/**
 * A client for the Help Tip REST API.
 */
public abstract class HelpTipApiClient {

    private final String baseUrl;
    private final String defaultUsername;
    private final String defaultPassword;
    private final UserRequestSender userRequestSender;

    public HelpTipApiClient(TestedProduct product, TestUser user) {
        this.baseUrl = product.getProductInstance().getBaseUrl();
        this.defaultUsername = user.getUsername();
        this.defaultPassword = user.getPassword();
        this.userRequestSender = new UserRequestSender(baseUrl);
    }

    public abstract void dismissAllHelpTips() throws Exception;

    protected void dismissHelpTip(String tipId) throws Exception {
        HttpPost request = new HttpPost(baseUrl + "/rest/helptips/1.0/tips");
        request.setEntity(new StringEntity(new JSONObject(ImmutableMap.<String, Object>of("id", tipId)).toString(), ContentType.APPLICATION_JSON));
        userRequestSender.sendRequestAsUser(request, new BasicResponseHandler(), defaultUsername, defaultPassword);
    }
}
