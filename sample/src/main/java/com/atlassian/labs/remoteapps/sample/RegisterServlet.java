package com.atlassian.labs.remoteapps.sample;

import net.oauth.OAuthServiceProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

import static com.atlassian.labs.remoteapps.sample.HttpServer.getHostBaseUrl;
import static com.atlassian.labs.remoteapps.sample.HttpServer.getOurBaseUrl;
import static com.atlassian.labs.remoteapps.sample.HttpUtils.render;

/**
 *
 */
public class RegisterServlet extends HttpServlet
{
    private final String appKey;
    private final String template;

    public RegisterServlet(String appKey, String templateType)
    {
        this.appKey = appKey;
        this.template = "sample-descriptor-" + templateType + ".mu.xml";
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

        final String output = render(template, new HashMap<String,Object>() {{
            put("baseurl", getOurBaseUrl());
            put("appkey", appKey);
            put("appbaseurl", getHostBaseUrl());
        }});
        byte[] bytes = output.getBytes(Charset.forName("UTF-8"));
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().close();
    }

}
