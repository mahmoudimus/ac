package com.atlassian.plugin.connect.test.pageobjects.confluence;

import java.io.IOException;
import java.net.URISyntaxException;

import com.atlassian.confluence.pageobjects.page.ConfluenceLoginPage;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.protocol.BasicHttpContext;

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
        String baseUrl = OwnerOfTestedProduct.INSTANCE.getProductInstance().getBaseUrl();

        java.net.URI uri = null;
        try
        {
            uri = new java.net.URI(baseUrl);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        DefaultHttpClient httpclient = new DefaultHttpClient(new SingleClientConnManager());
        httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

        HttpPost post = new HttpPost(baseUrl + "/rest/stp/1.0/license/remindMeNever");

        post.addHeader("Accept", "*/*");

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        try
        {
            String response = httpclient.execute(targetHost,post,responseHandler,localcontext);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        super.login(username,password,rememberMe);
    }
}
