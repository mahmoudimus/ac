package com.atlassian.labs.remoteapps.container.services;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.api.services.impl.AbstractOauthSignedRequestHandler;
import com.atlassian.labs.remoteapps.container.internal.Environment;
import net.oauth.*;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.RSA_SHA1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ContainerOAuthSignedRequestHandler extends AbstractOauthSignedRequestHandler implements SignedRequestHandler
{
    private static final Logger log = LoggerFactory.getLogger(ContainerOAuthSignedRequestHandler.class);
    // lazily loaded
    private volatile OAuthConsumer local;

    private final String appKey;
    protected final Environment env;

    public ContainerOAuthSignedRequestHandler(String appKey, Environment env)
    {
        this.appKey = appKey;
        this.env = env;
    }

    @Override
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

    private OAuthConsumer loadLocalConsumer()
    {
        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(null, null, null);
        OAuthConsumer localConsumer = new OAuthConsumer(null, appKey, null, serviceProvider);
        String privateKey = env.getEnv("OAUTH_LOCAL_PRIVATE_KEY");
        log.debug("Loaded private key:\n" + privateKey);
        localConsumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
        String publicKey = env.getEnv("OAUTH_LOCAL_PUBLIC_KEY");
        log.debug("Loaded public key:\n" + publicKey);
        localConsumer.setProperty(RSA_SHA1.PUBLIC_KEY, publicKey);
        return localConsumer;
    }

    public void addHost(String key, String publicKey, String baseUrl)
    {
        env.setEnv("OAUTH_HOST_PUBLIC_KEY." + key, publicKey);
        env.setEnv("HOST_BASE_URL." + key, baseUrl);
    }

    @Override
    protected String getHostOauthPublicKey(String key)
    {
        return env.getEnv("OAUTH_HOST_PUBLIC_KEY." + key);
    }

    @Override
    public String getHostBaseUrl(String key)
    {
        return env.getEnv("HOST_BASE_URL." + key);
    }

    @Override
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



    public void setLocalOauthKey(String key)
    {
        env.setEnvIfNull("OAUTH_LOCAL_KEY", key);
    }
}
