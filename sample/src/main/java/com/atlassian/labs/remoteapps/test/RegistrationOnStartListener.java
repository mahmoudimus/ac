package com.atlassian.labs.remoteapps.test;

import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 *
 */
public class RegistrationOnStartListener implements LifecycleAware, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(RegistrationOnStartListener.class);
    public static final String SECRET_TOKEN = "token";
    public static final String BASEURL = System.getProperty("baseurl").replace("localhost", "127.0.0.1");

    private volatile boolean started = false;
    private volatile boolean enabled = false;

    private final PluginEventManager pluginEventManager;

    public RegistrationOnStartListener(PluginEventManager pluginEventManager)
    {
        this.pluginEventManager = pluginEventManager;
        pluginEventManager.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(this);
    }

    @Override
    public void onStart()
    {
        started = true;
        if (enabled)
        {
            register();
        }
    }

    @PluginEventListener
    public void onEnable(PluginEnabledEvent event)
    {
        if ("remoteapp-installer".equals(event.getPlugin().getKey()))
        {
            enabled = true;
            if (started)
            {
                register();
            }
        }
    }

    private void register()
    {
        Thread t = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                try
                {
                    httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));
                    URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")), "UTF-8");
                    HttpPost post = new HttpPost(BASEURL + "/rest/remoteapps/latest/installer?" +
                        URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")), "UTF-8"));

                    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                    formparams.add(new BasicNameValuePair("url", BASEURL + "/remoteapp/register"));
                    formparams.add(new BasicNameValuePair("token", SECRET_TOKEN));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                    post.setEntity(entity);

                    log.error("registering remoteapp...");
                    // Create a response handler
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    String responseBody = httpclient.execute(post, responseHandler);
                    log.error("Registered.  Response:");
                    log.error("----------------------------------------");
                    log.error(responseBody);
                    log.error("----------------------------------------");

                }
                catch (ClientProtocolException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                catch (IOException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                finally
                {
                    // When HttpClient instance is no longer needed,
                    // shut down the connection manager to ensure
                    // immediate deallocation of all system resources
                    httpclient.getConnectionManager().shutdown();
                }
            }
        });
        t.start();
    }
}
