package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Registers calling applications only if the secret matches.  Uses a mutable environment
 * to store values.
 */
public class RegistrationServlet extends HttpServlet
{
    private final byte[] descriptor;
    private final String secret;
    private final OAuthContext oAuthContext;
    private static final Logger log = LoggerFactory.getLogger(RegistrationServlet.class);

    public RegistrationServlet(Document descriptor, Environment environment,
            OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
        this.secret = environment.getOptionalEnv("REGISTRATION_SECRET", "");

        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        XMLWriter xmlWriter = null;
        try
        {
            xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
        }
        catch (UnsupportedEncodingException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
        try
        {
            xmlWriter.write(descriptor);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to write node", e);
        }
        this.descriptor = writer.toByteArray();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null)
        {
            String oauthKey = req.getParameter("key");
            if (("RemoteAppsRegistration secret=" + this.secret).equals(authHeader))
            {
                String publicKey = req.getParameter("publicKey");
                String baseUrl = req.getParameter("baseUrl");

                log.info("Registering host - key: '{}' publicKey: '{}' baseUrl: '{}'",
                        new Object[]{oauthKey, publicKey, baseUrl});
                oAuthContext.addHost(oauthKey, publicKey, baseUrl);
            }
            else
            {
                log.warn("Rejecting registration from '{}' as the secret doesn't match: '{}'",
                        oauthKey, authHeader);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        resp.setContentType("text/xml");
        resp.setContentLength(descriptor.length);
        resp.getOutputStream().write(descriptor);
        resp.getOutputStream().close();
    }
}
