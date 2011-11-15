package com.atlassian.labs.remoteapps.test.remoteapp;

import com.atlassian.labs.remoteapps.test.RegistrationOnStartListener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class MyAdminRoute extends RemoteAppFilter.Route
{
    public MyAdminRoute(String path)
    {
        super(path);
    }

    @Override
    String handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        resp.setContentType("text/html");
        String consumerKey = OAuthContext.INSTANCE.validate2LOFromParameters(req);
        final Map<String, Object> context = newHashMap();
        context.put("consumerKey", consumerKey);
        context.put("remoteUser", getCurrentRemoteUser(req.getParameter("user_id")));
        context.put("forbiddenGet", getForbiddenStatusCode(req.getParameter("user_id")));
        context.put("baseurl", RegistrationOnStartListener.HOST_BASEURL);
        return render("test-page.vm", context);
    }

    String getCurrentRemoteUser(String userId)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try
        {
            String baseurl = RegistrationOnStartListener.HOST_BASEURL;
            HttpGet get = new HttpGet(baseurl + "/rest/remoteapptest/latest/?user_id=" + userId);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            OAuthContext.INSTANCE.sign(get);
            return httpclient.execute(get, responseHandler);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot retrieve remote user info", e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }

    String getForbiddenStatusCode(String userId)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try
        {
            String baseurl = RegistrationOnStartListener.HOST_BASEURL;
            HttpGet get = new HttpGet(baseurl + "/rest/remoteappforbidden/latest/?user_id=" + userId);
            ResponseHandler<String> responseHandler = new ExpectFailureResponseHandler();
            OAuthContext.INSTANCE.sign(get);
            return httpclient.execute(get, responseHandler);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot retrieve remote user info", e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }

    private static class ExpectFailureResponseHandler implements ResponseHandler<String>
    {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException
        {
            StatusLine statusLine = response.getStatusLine();
            return String.valueOf(statusLine.getStatusCode());
        }
    }


}
