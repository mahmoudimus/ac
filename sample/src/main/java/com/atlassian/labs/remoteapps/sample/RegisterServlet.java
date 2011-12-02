package com.atlassian.labs.remoteapps.sample;

import net.oauth.OAuthServiceProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import static com.atlassian.labs.remoteapps.sample.HttpServer.getOurBaseUrl;
import static com.atlassian.labs.remoteapps.sample.HttpUtils.render;

/**
 *
 */
public class RegisterServlet extends HttpServlet
{
    private final String appKey;
    private final String accessLevel;

    public RegisterServlet(String appKey, String accessLevel)
    {
        this.appKey = appKey;
        this.accessLevel = accessLevel;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/xml");
        OAuthContext.INSTANCE
                .setHost(req.getParameter("key"),
                        req.getParameter("publicKey"),
                        new OAuthServiceProvider(req.getParameter("requestTokenUrl"),
                                req.getParameter("authorizeUrl"),
                                req.getParameter("accessTokenUrl")));

        final String output = render("sample-descriptor.mu.xml", new HashMap<String,Object>() {{
            put("baseurl", getOurBaseUrl());
            put("appkey", appKey);
            put("accessLevel", accessLevel);
        }});
        byte[] bytes = output.getBytes(Charset.forName("UTF-8"));
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().close();
    }

}
