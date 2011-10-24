package com.atlassian.labs.remoteapps.test.remoteapp2;

import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
import com.atlassian.sal.api.net.*;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Set;

/**
 *
 */
public class RemoteApp2Rego implements DisposableBean
{

    private static final Logger log = LoggerFactory.getLogger(RemoteApp2Servlet.class);
    public static final String SECRET_TOKEN = "token";
    public static final String BASEURL = System.getProperty("baseurl").replace("localhost", "127.0.0.1");
    private final PluginEventManager pluginEventManager;
    private final RequestFactory requestFactory;
    public RemoteApp2Rego(PluginEventManager pluginEventManager, RequestFactory requestFactory)
    {
        this.pluginEventManager = pluginEventManager;
        this.requestFactory = requestFactory;
        pluginEventManager.register(this);
    }

    @PluginEventListener
    public void onPluginFrameworkStartedEvent(PluginFrameworkStartedEvent event)
    {
        register();
    }

    public void register()
    {
        Request request = requestFactory.createRequest(Request.MethodType.POST, BASEURL + "/rest/remoteapps/latest/installer");
        request.addRequestParameters("url", BASEURL + "/plugins/servlet/remoteapp2/register");
        request.addRequestParameters("token", SECRET_TOKEN);
        request.addBasicAuthentication("admin", "admin");
        try
        {
            request.execute(new ResponseHandler() {
                @Override
                public void handle(Response response) throws ResponseException
                {
                }
            });
        }
        catch (ResponseException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void destroy()
    {
        pluginEventManager.unregister(this);
    }
}
