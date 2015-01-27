package com.atlassian.plugin.connect.test.client;

import cc.plural.jsonij.JSON;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONObject;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public final class AtlassianConnectRestClient
{
    private final String baseUrl;
    private final String defaultUsername;
    private final String defaultPassword;
    private final UserRequestSender userRequestSender;

    public static final String UPM_URL_PATH = "/rest/plugins/1.0/";

    public AtlassianConnectRestClient(String baseUrl, String username, String password)
    {
        this.baseUrl = baseUrl;
        this.defaultUsername = username;
        this.defaultPassword = password;
        this.userRequestSender = new UserRequestSender(baseUrl);
    }

    public void install(String registerUrl) throws Exception
    {
        install(registerUrl, true);
    }

    // this variant is useful when testing install failure scenarios. i.e. where we expect the install to fail
    // It will timeout much quicker and swallow the exception that would terminate the test otherwise
    public void install(String registerUrl, boolean checkStatus) throws Exception
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

                if (checkStatus)
                {
                    InstallStatusChecker statusChecker = new InstallStatusChecker(userRequestSender, statusUrl, 1, TimeUnit.MINUTES, 500, TimeUnit.MILLISECONDS);
                    statusChecker.run(defaultUsername, defaultPassword);
                }
                else
                {
                    InstallStatusChecker statusChecker = new InstallStatusChecker(userRequestSender, statusUrl, 5, TimeUnit.SECONDS,
                            500, TimeUnit.MILLISECONDS);
                    try
                    {
                        statusChecker.run(defaultUsername, defaultPassword);
                    }
                    catch (Exception e)
                    {
                    }

                }
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

    public void setEnabled(String appKey, boolean enabled) throws Exception
    {
        HttpPut request = new HttpPut(UpmTokenRequestor.getUpmPluginResource(baseUrl, appKey));
        request.setHeader("Content-Type", "application/vnd.atl.plugins.plugin+json");
        String requestBody = new JSONObject(ImmutableMap.<String, Object>of("enabled", Boolean.toString(enabled))).toString();
        request.setEntity(new StringEntity(requestBody));
        request.setHeader("Accept", "application/json");

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        userRequestSender.sendRequestAsUser(request, responseHandler, defaultUsername, defaultPassword);
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
