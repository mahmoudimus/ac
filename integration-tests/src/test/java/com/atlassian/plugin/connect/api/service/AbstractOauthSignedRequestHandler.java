package com.atlassian.plugin.connect.api.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth.*;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.RSA_SHA1;

/**
 */
public abstract class AbstractOauthSignedRequestHandler
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    public String validateRequest(HttpServletRequest req) throws ServletException
    {
        URI requestUri = URI.create(req.getRequestURI());
        String url =getFullUrl(req);
        if (requestUri.getFragment() != null)
        {
            url += "#" + requestUri.getFragment();
        }
        if (req.getHeader("Authorization") != null)
        {
            OAuthMessage message = OAuthServlet.getMessage(req, url);
            return validateAndExtractKey(message);
        }
        else
        {
            final OAuthMessage message = new OAuthMessage(req.getMethod(), url, convertToSingleValues(url, getRequestParameters(req)).entrySet());
            return validateAndExtractKey(message);
        }
    }

    private OAuthConsumer getHostConsumer(String key)
    {
        String baseUrl = getHostBaseUrl(key);
        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(
                baseUrl + "/plugins/servlet/oauth/request-token",
                baseUrl + "/plugins/servlet/oauth/authorize",
                baseUrl + "/plugins/servlet/oauth/access-token");
        OAuthConsumer host = new OAuthConsumer(null, key, null, serviceProvider);
        host.setProperty(RSA_SHA1.PUBLIC_KEY, getHostOauthPublicKey(key));
        return host;
    }

    private String getFullUrl(HttpServletRequest req)
    {
        String contextPath = URI.create(getLocalBaseUrl()).getPath();
        String url = req.getRequestURI();
        if (url.startsWith(contextPath))
        {
            url = url.substring(contextPath.length());
        }
        url = getLocalBaseUrl() + url;
        return url;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String[]> getRequestParameters(HttpServletRequest req)
    {
        return req.getParameterMap();
    }


    private Map<String,String> convertToSingleValues(String url, Map<String, String[]> params)
    {
        Map<String,String> result = new HashMap<String,String>();
        for (Map.Entry<String,String[]> param : params.entrySet())
        {
            if (param.getValue().length > 1)
            {
                throw new IllegalArgumentException(String.format("Must not have multiples of query parameters.\nFound issue for URL %s, and parameter %s, with values %s", url, param.getKey(), Arrays.toString(
                        param.getValue())));
            }
            result.put(param.getKey(), param.getValue()[0]);
        }
        return result;
    }

    private String validateAndExtractKey(OAuthMessage message) throws ServletException
    {
        printMessage(message);
        try
        {
            OAuthConsumer host = getHostConsumer(message.getConsumerKey());
            message.validateMessage(new OAuthAccessor(host), new SimpleOAuthValidator());
            return message.getConsumerKey();
        }
        catch (OAuthProblemException ex)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation failed: \n");
            sb.append("problem: ").append(ex.getProblem()).append("\n");
            sb.append("parameters: ").append(ex.getParameters()).append("\n");
            System.err.println(sb.toString());
            throw new ServletException(ex);
        }
        catch (OAuthException e)
        {
            throw new ServletException(e);
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
        catch (URISyntaxException e)
        {
            throw new ServletException(e);
        }
    }

    private void printMessage(OAuthMessage message)
    {
        StringBuilder sb = new StringBuilder("Validating incoming OAuth request for remote plugin:\n");
        sb.append("\turl: ").append(message.URL.toString()).append("\n");
        sb.append("\tmethod: ").append(message.method.toString()).append("\n");
        try
        {
            for (Map.Entry<String,String> entry : message.getParameters())
            {
                sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        log.debug(sb.toString());
    }

    public void sign(URI uri, String method, String username, HttpURLConnection yc)
    {
        String authorization = getAuthorizationHeaderValue(uri, method, username);
        yc.setRequestProperty("Authorization", authorization);
    }

    protected abstract String getAuthorizationHeaderValue(URI uri, String method, String username);

    protected abstract String getLocalBaseUrl();

    protected abstract Object getHostOauthPublicKey(String key);

    protected abstract String getHostBaseUrl(String key);
}
