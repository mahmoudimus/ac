package com.atlassian.labs.remoteapps.test.remoteapp;

import com.atlassian.labs.remoteapps.test.RegistrationOnStartListener;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 *
 */
public class RegisterRoute extends RemoteAppFilter.Route
{
    private static final Logger log = LoggerFactory.getLogger(RegisterRoute.class);
    private static final Set<String> POST_PARAMS = ImmutableSet.of("token", "key", "publicKey", "description", "requestTokenUrl", "accessTokenUrl", "authorizeUrl");
    public RegisterRoute(String path)
    {
        super(path);
    }

    @Override
    public String handle(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        resp.setContentType("text/xml");
        return replaceTokens(req, IOUtils.toString(getClass().getResourceAsStream("/atlassian-plugin-remoteapp.xml")));
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
        originalXml = originalXml.replaceAll("%baseurl%", RegistrationOnStartListener.BASEURL);
        return originalXml;
    }
}
