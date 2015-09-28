package com.atlassian.plugin.connect.test.server;

import com.atlassian.plugin.connect.test.Environment;

public class RunnerSignedRequestHandler extends ContainerOAuthSignedRequestHandler
{
    public RunnerSignedRequestHandler(String appKey, Environment env)
    {
        super(appKey, env);
    }

    public String getPublicKey()
    {
        return env.getEnv("OAUTH_LOCAL_PUBLIC_KEY");
    }
}
