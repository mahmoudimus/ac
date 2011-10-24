package com.atlassian.labs.remoteapps.test.remoteapp2;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 */
public class RemoteApp2Servlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(RemoteApp2Servlet.class);
    private static final Set<String> POST_PARAMS = ImmutableSet.of(
            "token",
            "key",
            "publicKey",
            "description",
            "requestTokenUrl",
            "accessTokenUrl",
            "authorizeUrl");
    private final RemoteApp2Rego register;

    public RemoteApp2Servlet(RemoteApp2Rego register)
    {
        this.register = register;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        register.register();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/xml");
        final String pluginXml = replaceTokens(req, IOUtils.toString(getClass().getResourceAsStream("/atlassian-plugin-remoteapp2.xml")));

        PrintWriter out = resp.getWriter();

        out.print(pluginXml);
        out.close();
    }

    private String replaceTokens(HttpServletRequest req, String originalXml) throws ServletException
    {
        for (String name : POST_PARAMS)
        {
            String value = req.getParameter(name);
            if (value == null)
            {
                throw new ServletException("parameter " + name + " is required");
            }
            log.info("param: " + name + " : " + value);
            originalXml = originalXml.replaceAll("%" + name + "%", value);
        }
        originalXml = originalXml.replaceAll("%baseurl%", RemoteApp2Rego.BASEURL);
        return originalXml;
    }


}
