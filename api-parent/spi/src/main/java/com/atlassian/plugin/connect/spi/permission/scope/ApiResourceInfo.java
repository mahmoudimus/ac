package com.atlassian.plugin.connect.spi.permission.scope;

/**
 *
 */
public final class ApiResourceInfo
{
    private final String path;
    private final String httpMethod;
    private final String rpcMethod;

    public ApiResourceInfo(String path, String httpMethod)
    {
        this(path, httpMethod, null);
    }

    public ApiResourceInfo(String path, String httpMethod, String rpcMethod)
    {
        this.path = path;
        this.httpMethod = httpMethod;
        this.rpcMethod = rpcMethod;
    }

    public String getPath()
    {
        return path;
    }

    public String getHttpMethod()
    {
        return httpMethod;
    }

    public String getRpcMethod()
    {
        return rpcMethod;
    }
}
