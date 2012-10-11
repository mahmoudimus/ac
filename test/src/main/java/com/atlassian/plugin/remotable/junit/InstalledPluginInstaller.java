package com.atlassian.plugin.remotable.junit;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;


final class InstalledPluginInstaller implements PluginInstaller
{
    @Override
    public void start(String... apps)
    {
        for (String app : apps)
        {
            legacyUpload(app);
//            upload(app);
        }
    }

    @Override
    public void stop()
    {
        // uninstall, we don't care for now
    }

    private void legacyUpload(String app)
    {

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        String url = System.getProperty("baseurl") + "/admin/uploadplugin.action" + "?os_username=" + urlEncode("admin") + "&os_password=" + urlEncode("admin");

        MultipartPostMethod filePost = new MultipartPostMethod(url);
        filePost.setDoAuthentication(true);
        filePost.setFollowRedirects(true);

        // bypass anti xsrf protection
        filePost.setRequestHeader("X-Atlassian-Token", "no-check");

        try
        {
            File pluginFile = new File(app);
            filePost.addParameter("file_0", pluginFile.getName(), pluginFile);
            client.setConnectionTimeout(5000);
            // Execute the method.

//            getLog().info( getTitle() + ": Uploading '" + pluginFile.getName() + "' to server: " + serverUrl );
            int status = client.executeMethod(filePost.getHostConfiguration(), filePost, getHttpState());

            if (status == HttpStatus.SC_MOVED_TEMPORARILY)
            {
                Header location = filePost.getResponseHeader("Location");
                if (location == null)
                {
                    throw new IllegalStateException();
                }
                else if (location.getValue().indexOf("/login.action") >= 0)
                {
                    throw new IllegalStateException();
                }
            }
            else if (status != HttpStatus.SC_OK)
            {

                throw new IllegalStateException();
            }
        }
        catch (ConnectException e)
        {
            throw new IllegalStateException(e);
            // getLog().debug(e);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
        catch (HttpException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            filePost.releaseConnection();
        }
    }

    protected HttpState getHttpState()
    {
        HttpState httpState = new HttpState();
        try
        {
            httpState.setCredentials(null, new URI(System.getProperty("baseurl")).getHost(), new UsernamePasswordCredentials("admin", "admin"));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Bad serverUrl:" + System.getProperty("baseurl"), e);
        }
        httpState.setAuthenticationPreemptive(true);
        return httpState;
    }


    protected String urlEncode(String text)
    {

        try
        {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return text;
        }
    }
}
