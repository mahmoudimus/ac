package com.atlassian.plugin.connect.plugin.installer;

public class AddonSettings
{
    private String descriptor;
    private String baseUrl;
    private String secret;
    private String user;
    private String auth;

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

    public String getUser()
    {
        return user;
    }

    public AddonSettings setUser(String user)
    {
        this.user = user;

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
}
