package com.atlassian.labs.remoteapps.sample.test;

import com.atlassian.labs.remoteapps.sample.HttpServer;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    public static final String HOST_BASEURL = System.getProperty("baseurl");
    private static final URI APP_BASEURL;

    private volatile boolean started = false;
    private volatile boolean enabled = false;

    private final PluginEventManager pluginEventManager;
    private final ApplicationProperties applicationProperties;
    private HttpServer server;

    static
    {
        URI host = URI.create(HOST_BASEURL);
        URI url = null;
        try
        {
            url = new URI(host.getScheme(), host.getUserInfo(), host.getHost(), host.getPort() + 1, null, null, null);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        APP_BASEURL = url;

    }

    public RegistrationOnStartListener(PluginEventManager pluginEventManager,
                                       ApplicationProperties applicationProperties
    )
    {
        this.pluginEventManager = pluginEventManager;
        this.applicationProperties = applicationProperties;
        pluginEventManager.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        pluginEventManager.unregister(this);
        if (server != null)
        {
            server.stop();
        }
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
        if ("sample-installer".equals(event.getPlugin().getKey()))
        {
            enabled = true;
            if (started)
            {
                register();
            }
        }
    }

    private void startRemoteApp()
    {
        server = new HttpServer("app1", HOST_BASEURL, APP_BASEURL.toString(), APP_BASEURL.getPort());
        server.start();
    }

    private void register()
    {
        startRemoteApp();

        Thread t = new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                try
                {
                    httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("betty", "betty"));
                    URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")), "UTF-8");
                    HttpPost post = new HttpPost(HOST_BASEURL + "/rest/remoteapps/latest/installer?" +
                        URLEncodedUtils.format(singletonList(new BasicNameValuePair("os_authType", "basic")), "UTF-8"));

                    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                    formparams.add(new BasicNameValuePair("url", getRegistrationUrl()));
                    formparams.add(new BasicNameValuePair("token", SECRET_TOKEN));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                    post.setEntity(entity);

                    log.error("registering app1 via '" + getRegistrationUrl() + "'...");
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
                    throw new RuntimeException(e);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
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

    private String getRegistrationUrl()
    {
        String product = System.getProperty("product", "refapp");
        return APP_BASEURL + "/" + product + "-register";
    }
}
