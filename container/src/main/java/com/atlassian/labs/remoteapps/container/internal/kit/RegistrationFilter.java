package com.atlassian.labs.remoteapps.container.internal.kit;

import com.atlassian.labs.remoteapps.host.common.descriptor.DescriptorAccessor;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;
import com.atlassian.labs.remoteapps.container.internal.Environment;
import com.atlassian.labs.remoteapps.container.service.ContainerOAuthSignedRequestHandler;
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
    private final String secret;
    private final DescriptorAccessor descriptorAccessor;
    private final ContainerOAuthSignedRequestHandler requestHandler;
    private static final Logger log = LoggerFactory.getLogger(RegistrationFilter.class);

    public RegistrationFilter(DescriptorAccessor descriptor, Environment environment, SignedRequestHandler requestHandler)
    {
        this.descriptorAccessor = descriptor;
        this.requestHandler = (ContainerOAuthSignedRequestHandler) requestHandler;
        this.secret = environment.getOptionalEnv("REGISTRATION_SECRET", "");
    }

    private byte[] readDescriptorToBytes()
    {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        XMLWriter xmlWriter;
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
            xmlWriter.write(descriptorAccessor.getDescriptor());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to write node", e);
        }
        return writer.toByteArray();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
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
                String productType = req.getParameter("productType");

                log.info("Registering host - key: '{}' baseUrl: '{}'", oauthKey, baseUrl);
                requestHandler.addHost(oauthKey, publicKey, baseUrl, productType);
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
        byte[] descriptor = readDescriptorToBytes();
        resp.setContentLength(descriptor.length);
        resp.getOutputStream().write(descriptor);
        resp.getOutputStream().close();
    }

    @Override
    public void destroy()
    {
    }
}
