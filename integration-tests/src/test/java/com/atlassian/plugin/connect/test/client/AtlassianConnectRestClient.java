package com.atlassian.plugin.connect.test.client;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.Value;

public final class AtlassianConnectRestClient
{
    private final String baseUrl;
    private final String defaultUsername;
    private final String defaultPassword;
    private final UserRequestSender userRequestSender;

    public static final String UPM_URL_PATH = "/rest/plugins/1.0/";
    private static final String UPM_TOKEN_HEADER = "upm-token";
    private static final Random RAND = new Random();

    public AtlassianConnectRestClient(String baseUrl, String username, String password)
    {
        this.baseUrl = baseUrl;
        this.defaultUsername = username;
        this.defaultPassword = password;
        this.userRequestSender = new UserRequestSender(baseUrl);
    }

    public void install(String registerUrl) throws Exception
    {
        //get a upm token
        String token = getUpmToken();

        HttpPost post = new HttpPost(baseUrl + UPM_URL_PATH + "?token=" + token);

        post.addHeader("Accept", "application/json");
        post.setEntity(new StringEntity("{ \"pluginUri\": \"" + registerUrl + "\", \"pluginName\": \"the plugin name\" }", ContentType.create("application/vnd.atl.plugins.install.uri+json")));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        String response = userRequestSender.sendRequestAsUser(post, responseHandler, defaultUsername, defaultPassword);

        if(Strings.isNullOrEmpty(response) || (!response.startsWith("{") && !response.endsWith("}")))
        {
            install(registerUrl);
        }
        else
        {
            JSON json = JSON.parse(response);
    
            if (null == json.get("enabled"))
            {
                URI uri = new URI(baseUrl);
                final String statusUrl = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + json.get("links").get("self").getString();
    
                InstallStatusChecker statusChecker = new InstallStatusChecker(userRequestSender, statusUrl, 1, TimeUnit.MINUTES, 500, TimeUnit.MILLISECONDS);
                statusChecker.run(defaultUsername, defaultPassword);
            }
        }
    }

    public void uninstall(String appKey) throws Exception
    {
        HttpDelete delete = new HttpDelete(UpmTokenRequestor.getUpmPluginResource(baseUrl, appKey));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        
        try
        {
            userRequestSender.sendRequestAsUser(delete, responseHandler, defaultUsername, defaultPassword);
        }
        catch (HttpResponseException e)
        {
            //eat 404's as it means the addon does not exist
            if(e.getStatusCode() != 404)
            {
                throw e;
            }
        }
    }

    public String getUpmPluginJson(String appKey) throws Exception
    {
        HttpGet get = new HttpGet(UpmTokenRequestor.getUpmPluginResource(baseUrl, appKey));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        return userRequestSender.sendRequestAsUser(get, responseHandler, defaultUsername, defaultPassword);
    }

    private String getUpmToken() throws Exception
    {
        UpmTokenRequestor tokenRequestor = new UpmTokenRequestor(userRequestSender, 1, TimeUnit.MINUTES, 500, TimeUnit.MILLISECONDS);
        return tokenRequestor.run(defaultUsername, defaultPassword);
    }


}
