package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.page.ConfluenceLoginPage;
import com.atlassian.plugin.connect.test.BaseUrlLocator;
import com.atlassian.plugin.connect.test.client.AtlassianConnectRestClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;

public class FixedConfluenceLoginPage extends ConfluenceLoginPage
{
    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }

    @Override
    public void login(String username, String password, boolean rememberMe)
    {
        //ALL of this is a shitty hack to get around the licese reminder confluence pops up over the main nav
        String baseUrl = BaseUrlLocator.getBaseUrl();
        AtlassianConnectRestClient client = new AtlassianConnectRestClient(baseUrl,username,password);
        
        HttpPost post = new HttpPost(baseUrl + "/rest/stp/1.0/license/remindMeNever");
        post.addHeader("Accept", "*/*");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        try
        {
            String response = client.sendRequestAsUser(post,responseHandler,username,password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.login(username,password,rememberMe);
    }
}
