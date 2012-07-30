package com.atlassian.labs.remoteapps.test;

import com.atlassian.labs.remoteapps.container.internal.Environment;
import com.atlassian.labs.remoteapps.container.services.ContainerOAuthSignedRequestHandler;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/29/12 Time: 11:38 PM To change this template use
 * File | Settings | File Templates.
 */
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
