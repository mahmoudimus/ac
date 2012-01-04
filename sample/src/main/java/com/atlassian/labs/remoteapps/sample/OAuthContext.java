package com.atlassian.labs.remoteapps.sample;

import com.atlassian.labs.remoteapps.sample.junit.XmlRpcClient;
import net.oauth.*;
import net.oauth.signature.RSA_SHA1;
import org.apache.axis.client.Stub;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.transport.http.HTTPConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 *
 */
public class OAuthContext
{
    public static OAuthContext INSTANCE;

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

    public static void init(String appKey, String ourBaseUrl)
    {
        INSTANCE = new OAuthContext(appKey, ourBaseUrl);
    }

    public OAuthContext(String appKey, String baseUrl)
    {

        local = new OAuthConsumer(null, // our callback
                appKey, null, // certs used instead
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
        String url = HttpServer.getOurBaseUrl() + URI.create(req.getRequestURI()).getPath();
        OAuthMessage message = new OAuthMessage(req.getMethod(), url,
                convertToSingleValues(req.getParameterMap()).entrySet());
        return validateAndExtractKey(message);
    }

    private Map<String,String> convertToSingleValues(Map<String,String[]> params)
    {
        Map<String,String> result = new HashMap<String,String>();
        for (Map.Entry<String,String[]> param : params.entrySet())
        {
            if (param.getValue().length > 1)
            {
                throw new IllegalArgumentException("Must not have multiples of query parameters");
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

    private String getAuthorizationHeaderValue(String uri, String method, final String user)
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
                put("user_id", user);
            }};
            OAuthMessage oauthMessage = new OAuthMessage(method, uri, params.entrySet());
            dumpOutgoingMessage(oauthMessage);
            oauthMessage.sign(new OAuthAccessor(local));
            return oauthMessage.getAuthorizationHeader(null);
        }
        catch (OAuthException e)
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

    private void dumpOutgoingMessage(OAuthMessage message) throws IOException
    {
        StringBuilder sb = new StringBuilder("Signing outgoing OAuth 2LO request:\n");
        sb.append("\turl: ").append(message.URL).append("\n");
        sb.append("\tmethod: ").append(message.method).append("\n");
        for (Map.Entry<String,String> entry : message.getParameters())
        {
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        System.out.println(sb.toString());
    }

    public void sign(String uri, String user, XmlRpcClient client)
    {
        String authorization = getAuthorizationHeaderValue(uri, "POST", user);
        client.setRequestProperty("Authorization", authorization);
    }

    public void sign(String uri, String user, Stub service)
    {
        String authorization = getAuthorizationHeaderValue(uri, "POST", user);
        service._setProperty(HTTPConstants.REQUEST_HEADERS, new Hashtable(singletonMap(HTTPConstants.HEADER_AUTHORIZATION, authorization)));
    }

    public void sign(String uri, String method, String username, HttpURLConnection yc)
    {
        String authorization = getAuthorizationHeaderValue(uri, method, username);
        yc.setRequestProperty("Authorization", authorization);
    }

    public String sendSignedGet(String uri, String user)
    {
        try
        {
            URL url = new URL(uri + "?user_id=" + user);
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();
            sign(uri, "GET", user, yc);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
                result.append("\n");
            }
            result.deleteCharAt(result.length() - 1);
            in.close();
            return result.toString();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public int sendFailedSignedGet(String uri, String user)
    {
        HttpURLConnection yc = null;
        try
        {
            URL url = new URL(uri + "?user_id=" + user);
            yc = (HttpURLConnection) url.openConnection();
            sign(uri, "GET", user, yc);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            return yc.getResponseCode();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            try
            {
                if (yc != null)
                {
                    return yc.getResponseCode();
                }
                throw new RuntimeException("no status code");
            }
            catch (IOException e1)
            {
                throw new RuntimeException(e);
            }
        }
    }

}
