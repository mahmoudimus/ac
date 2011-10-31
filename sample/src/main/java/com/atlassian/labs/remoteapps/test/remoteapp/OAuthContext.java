package com.atlassian.labs.remoteapps.test.remoteapp;

import com.atlassian.labs.remoteapps.test.RegistrationOnStartListener;
import com.google.common.collect.ImmutableMap;
import net.oauth.*;
import net.oauth.server.HttpRequestMessage;
import net.oauth.signature.RSA_SHA1;
import org.apache.http.client.methods.HttpGet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;

/**
 *
 */
public class OAuthContext
{
    public static final OAuthContext INSTANCE = new OAuthContext();

    private static final String PRIVATE_KEY =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIICXQIBAAKBgQCdZ4NjFq7phYw2FAvg2r3bS0VwkX07mBsYP5iPH0tsy9Boo4Pf\n" +
            "QTXxPrd6NFP6Gm0QXsgERgFgDt/344WhxMqt4rJiudINI51n0bXNz9ct5ZnUJSnw\n" +
            "C4GzNkDmvjpJ/Ewdmq5Ye3BfaThZnlsXA+csT3IU/strFeKjydl1lyXNswIDAQAB\n" +
            "AoGAJSmYUp+7YjT+mpH3D/p1Er5dwasH5zcNRpdVPI1F8ITaSqo4a1BpHPESvo52\n" +
            "OTleAJxwGtowXu6EIHGeTkg5FZ/947AeyxG9XsAYty+HF5SkEoY+Pw4Yy4ZfmIZq\n" +
            "FAll6SX13n+xtqiumVdFOo5ysRIdpy0sblbuOO0G+O/xuDECQQDNHldYFA054LO4\n" +
            "jLQrdrMQZnj7nZEqno9OtB1qbUE9Ghzrbb90K3ZTcegS0zcSgyz7n0PyLzH11KFF\n" +
            "9Lz+pd25AkEAxHMsyTCWO0vkVewk1sjkqKJettiSXFTkngWum2n9F2F6RooAByZc\n" +
            "1/FC8Kkdz1IJmuN9M8uQ+RDGc/IfgbTcywJBAJmyGXqbE8oBkElBzSMgP06TqiXH\n" +
            "zGWmB/XOSphbo124emECjEns4y3llSK99288sXEdxtjq+kGdAPcdSpx5BqkCQEOS\n" +
            "Eh+JlMMEkZ90QB+Yrf3LC6T8zSrxEEnCTpKqXCGEp9hHc0cCTQEBvTKmGNjMsP0T\n" +
            "rmb4Z/8jY/9RksC8gw0CQQCVs6qudqbQohIjbtkxMpEyutJo9iTsI2Rx8wZGAiRI\n" +
            "qwOHppn3z+U6SVDEv/RM8bpCkgmuhXkdP3w23Rojk3qP\n" +
            "-----END RSA PRIVATE KEY-----";

    private OAuthConsumer host = null;
    private final OAuthConsumer local;

    public OAuthContext()
    {
        final String baseUrl = RegistrationOnStartListener.OUR_BASEURL;

        local = new OAuthConsumer(null, // our callback
                "remoteapp", null, // certs used instead
                new OAuthServiceProvider(baseUrl + "/remoteapp/oauth/requestTokenUrl",
                        baseUrl + "/remoteapp/oauth/accessTokenUrl",
                        baseUrl + "/remoteapp/oauth/authorizeUrl"));
        local.setProperty(RSA_SHA1.PRIVATE_KEY, PRIVATE_KEY);
    }

    public void setHost(String consumerKey, String publicKey, OAuthServiceProvider serviceProvider)
    {
        host = new OAuthConsumer(null, consumerKey, null, serviceProvider);
        host.setProperty(RSA_SHA1.PUBLIC_KEY, publicKey);

    }

    public String validate2LOFromParameters(HttpServletRequest req) throws ServletException
    {
        OAuthMessage message = new OAuthMessage(req.getMethod(), req.getRequestURL().toString(),
                convertToSingleValues(req.getParameterMap()).entrySet());
        return validateAndExtractKey(message);
    }

    private Map<String,String> convertToSingleValues(Map<String,String[]> params)
    {
        Map<String,String> result = newHashMap();
        for (Map.Entry<String,String[]> param : params.entrySet())
        {
            result.put(param.getKey(), param.getValue()[0]);
        }
        return result;
    }

    public String validate2LOFromHeaders(HttpServletRequest req) throws ServletException
    {
        HttpRequestMessage message = new HttpRequestMessage(req, req.getRequestURL().toString());
        return validateAndExtractKey(message);
    }

    private String validateAndExtractKey(OAuthMessage message) throws ServletException
    {
        try
        {
            message.validateMessage(new OAuthAccessor(host), new SimpleOAuthValidator());
            return message.getConsumerKey();
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

    public void sign(HttpGet get)
    {
        try
        {
            final String timestamp = System.currentTimeMillis() / 1000 + "";
            final String nonce = System.nanoTime() + "";
            Map<String,String> params = new HashMap<String,String>() {{
                put(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
                put(OAuth.OAUTH_VERSION, "1.0");
                put(OAuth.OAUTH_CONSUMER_KEY, local.consumerKey);
                put(OAuth.OAUTH_NONCE, nonce);
                put(OAuth.OAUTH_TIMESTAMP, timestamp);
            }};
            OAuthMessage oauthMessage = new OAuthMessage(get.getMethod(), get.getURI().toString(), params.entrySet());
            oauthMessage.sign(new OAuthAccessor(local));
            get.addHeader("Authorization", oauthMessage.getAuthorizationHeader(null));
        }
        catch (net.oauth.OAuthException e)
        {
            // todo: do something better
            throw new RuntimeException("Failed to sign the request", e);
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
}
