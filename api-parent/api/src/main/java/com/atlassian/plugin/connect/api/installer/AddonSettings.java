package com.atlassian.plugin.connect.api.installer;


import com.atlassian.plugin.PluginState;

public class AddonSettings
{
    private String descriptor;
    private String baseUrl;
    private String secret;
    private String userKey;
    private String auth;
    private String restartState;

    public AddonSettings()
    {
        this.descriptor = "";
        this.baseUrl = "";
        this.secret = "";
        this.userKey = "";
        this.auth = "";
        this.restartState = PluginState.ENABLED.name();
    }

    public String getDescriptor()
    {
        return descriptor;
    }

    public AddonSettings setDescriptor(String descriptor)
    {
        this.descriptor = descriptor;
        
        return this;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public AddonSettings setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;

        return this;
    }

    public String getSecret()
    {
        return secret;
    }

    public AddonSettings setSecret(String secret)
    {
        this.secret = secret;

        return this;
    }

    public String getUserKey()
    {
        return userKey;
    }

    public AddonSettings setUserKey(String user)
    {
        this.userKey = user;

        return this;
    }

    public String getAuth()
    {
        return auth;
    }

    public AddonSettings setAuth(String auth)
    {
        this.auth = auth;

        return this;
    }

    public String getRestartState()
    {
        return restartState;
    }

    public AddonSettings setRestartState(PluginState restartState)
    {
        this.restartState = restartState.name();
        return this;
    }
}
