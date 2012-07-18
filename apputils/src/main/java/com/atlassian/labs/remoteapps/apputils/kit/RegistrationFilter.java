package com.atlassian.labs.remoteapps.apputils.kit;

import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Registers calling applications only if the secret matches.  Uses a mutable environment
 * to store values.
 */
public class RegistrationFilter implements Filter
{
    private final byte[] descriptor;
    private final String secret;
    private final OAuthContext oAuthContext;
    private static final Logger log = LoggerFactory.getLogger(RegistrationFilter.class);

    public RegistrationFilter(Document descriptor, Environment environment, OAuthContext oAuthContext)
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
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null)
        {
            String oauthKey = req.getParameter("key");
            if (("RemoteAppsRegistration secret=" + this.secret).equals(authHeader))
            {
                String publicKey = req.getParameter("publicKey");
                String baseUrl = req.getParameter("baseUrl");

                log.info("Registering host - key: '{}' baseUrl: '{}'", oauthKey, baseUrl);
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

    @Override
    public void destroy()
    {
    }
}
