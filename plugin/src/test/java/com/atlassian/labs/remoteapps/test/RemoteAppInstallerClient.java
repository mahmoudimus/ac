package com.atlassian.labs.remoteapps.test;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 17/02/12 Time: 9:47 PM To change this template use
 * File | Settings | File Templates.
 */
public class RemoteAppInstallerClient
{
    private final String baseUrl;
    private final DefaultHttpClient httpclient;

    public RemoteAppInstallerClient(String baseUrl, String username, String password)
    {
        this.baseUrl = baseUrl;
        httpclient = new DefaultHttpClient(new SingleClientConnManager());
        httpclient.getCredentialsProvider().setCredentials(
                AuthScope.ANY, new UsernamePasswordCredentials(username, password));
    }

    public void install(String registerUrl, String secret) throws IOException
    {
        HttpPost post = new HttpPost(baseUrl + "/rest/remoteapps/latest/installer?" +
                URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")),
                        "UTF-8"));

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("url", registerUrl));
        formparams.add(new BasicNameValuePair("token", secret));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(entity);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        httpclient.execute(post, responseHandler);
    }

    public void uninstall(String appKey) throws IOException
    {
        HttpDelete post = new HttpDelete(baseUrl + "/rest/remoteapps/latest/uninstaller/" + appKey + "?" +
                URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")), "UTF-8"));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        httpclient.execute(post, responseHandler);
    }
}
