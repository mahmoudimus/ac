package com.atlassian.plugin.remotable.test.server;

import com.atlassian.plugin.remotable.container.internal.Environment;
import com.atlassian.plugin.remotable.container.service.ContainerOAuthSignedRequestHandler;

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
