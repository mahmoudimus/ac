package com.atlassian.labs.remoteapps.apputils;

import net.oauth.*;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.RSA_SHA1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Helps with oauth operations
 */
public class OAuthContext
{
    // lazily loaded
    private volatile OAuthConsumer local;

    private final Environment env;

    public OAuthContext(Environment env)
    {
        this.env = env;
    }

    private OAuthConsumer loadLocalConsumer()
    {
        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(null, null, null);
        OAuthConsumer localConsumer = new OAuthConsumer(null, env.getEnv("OAUTH_LOCAL_KEY"), null, serviceProvider);
        String privateKey = env.getEnv("OAUTH_LOCAL_PRIVATE_KEY");
        System.out.println("Loaded private key:\n" + privateKey);
        localConsumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
        String publicKey = env.getEnv("OAUTH_LOCAL_PUBLIC_KEY");
        System.out.println("Loaded public key:\n" + publicKey);
        localConsumer.setProperty(RSA_SHA1.PUBLIC_KEY, publicKey);
        return localConsumer;
    }

    public void addHost(String key, String publicKey, String baseUrl)
    {
        env.setEnv("OAUTH_HOST_PUBLIC_KEY." + key, publicKey);
        env.setEnv("HOST_BASE_URL." + key, baseUrl);
    }

    public OAuthConsumer getHostConsumer(String key)
    {
        String baseUrl = getHostBaseUrl(key);
        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(
                                baseUrl + "/plugins/servlet/oauth/request-token",
                                baseUrl + "/plugins/servlet/oauth/authorize",
                                baseUrl + "/plugins/servlet/oauth/access-token");
        OAuthConsumer host = new OAuthConsumer(null, key, null, serviceProvider);
        host.setProperty(RSA_SHA1.PUBLIC_KEY, env.getEnv("OAUTH_HOST_PUBLIC_KEY." + key));
        return host;
    }

    public String getHostBaseUrl(String key)
    {
        return env.getEnv("HOST_BASE_URL." + key);
    }

    public String getLocalBaseUrl()
    {
        return env.getEnv("BASE_URL");
    }

    public void setLocalBaseUrlIfNull(String baseUrl)
    {
        env.setEnvIfNull("BASE_URL", baseUrl);
    }

    public OAuthConsumer getLocal()
    {
        if (local == null)
        {
            local = loadLocalConsumer();
        }
        return local;
    }

    public String validate2LOFromParameters(HttpServletRequest req) throws ServletException
    {
        final String url = getFullUrl(req);
        final OAuthMessage message = new OAuthMessage(req.getMethod(), url, convertToSingleValues(url, getRequestParameters(req)).entrySet());
        return validateAndExtractKey(message);
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
                throw new IllegalArgumentException(String.format("Must not have multiples of query parameters.\nFound issue for URL %s, and parameter %s, with values %s", url, param.getKey(), Arrays.toString(param.getValue())));
            }
            result.put(param.getKey(), param.getValue()[0]);
        }
        return result;
    }

    public String validateRequest(HttpServletRequest req) throws ServletException
    {
        URI requestUri = URI.create(req.getRequestURI());
        String url =getFullUrl(req);
        if (requestUri.getFragment() != null)
        {
            url += "#" + requestUri.getFragment();
        }
        OAuthMessage message = OAuthServlet.getMessage(req, url);
        return validateAndExtractKey(message);
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
        StringBuilder sb = new StringBuilder("Validating incoming OAuth request for sample remoteapp:\n");
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
        System.out.println(sb.toString());
    }

    public void sign(String uri, String method, String username, HttpURLConnection yc)
    {
        String authorization = getAuthorizationHeaderValue(uri, method, username);
        yc.setRequestProperty("Authorization", authorization);
    }

    public String getAuthorizationHeaderValue(String uri, String method, final String username)
            throws IllegalArgumentException
    {
        final OAuthConsumer local = getLocal();
        try
        {
            final String timestamp = System.currentTimeMillis() / 1000 + "";
            final String nonce = System.nanoTime() + "";
            Map<String,String> params = new HashMap<String,String>() {{
                put(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
                put(OAuth.OAUTH_VERSION, "1.0");
                put(OAuth.OAUTH_CONSUMER_KEY,
                        local.consumerKey);
                put(OAuth.OAUTH_NONCE, nonce);
                put(OAuth.OAUTH_TIMESTAMP, timestamp);
                if (username != null)
                {
                    put("user_id", username);
                }
            }};
            OAuthMessage oauthMessage = new OAuthMessage(method, uri, params.entrySet());
            oauthMessage.sign(new OAuthAccessor(local));
            return oauthMessage.getAuthorizationHeader(null);
        }
        catch (OAuthException e)
        {
            // shouldn't really happen...
            throw new IllegalArgumentException("Failed to sign the request", e);
        }
        catch (IOException e)
        {
            // this shouldn't happen as the message is not being read from any IO streams, but the OAuth library throws
            // these around like they're candy, but far less sweet and tasty.
            throw new RuntimeException(e);
        }
        catch (URISyntaxException e)
        {
            // this shouldn't happen unless the caller somehow passed us an invalid URI object
            throw new RuntimeException(e);
        }
    }

    public void setLocalOauthKey(String key)
    {
        env.setEnvIfNull("OAUTH_LOCAL_KEY", key);
    }
}
