package com.atlassian.labs.remoteapps.apputils;

import com.samskivert.mustache.Mustache;
import net.oauth.signature.RSA_SHA1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates the remote app descriptor passing in:
 * <ul>
 *     <li>appKey - The remote app key</li>
 *     <li>baseUrl - The base url of the remote app</li>
 *     <li>publickey - The public key of the remote app</li>
 * </ul>
 */
public class RegisterServlet extends HttpServlet
{
    private final String descriptorTemplate;
    private final OAuthContext oauthContext;

    public RegisterServlet(String descriptorTemplate, OAuthContext oauthContext)
    {
        this.descriptorTemplate = descriptorTemplate;
        this.oauthContext = oauthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // only allow setting of the env data if not running in Heroku
        if (req.getParameter("key") != null && System.getenv("DATABASE_URL") == null)
        {
            // we still set this dynamically for easier testing, although the change is still global
            oauthContext.setHost(req.getParameter("key"), req.getParameter("publicKey"), req.getParameter("baseUrl"));
        }

        resp.setContentType("text/xml");
        final String output = render(descriptorTemplate, new HashMap<String,Object>() {{
            put("appKey", oauthContext.getLocal().consumerKey);
            put("baseUrl", oauthContext.getLocalBaseUrl());
            put("publicKey", oauthContext.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY));
        }});
        byte[] bytes = output.getBytes(Charset.forName("UTF-8"));
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().close();
    }

    public String render(String template, Map<String,Object> context)
    {
        StringWriter writer = new StringWriter();
        Mustache.compiler().compile(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(template))).execute(context,
                writer);
        return writer.toString();
    }

}
