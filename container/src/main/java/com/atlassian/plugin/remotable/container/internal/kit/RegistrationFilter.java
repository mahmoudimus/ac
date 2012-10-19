package com.atlassian.plugin.remotable.container.internal.kit;

import com.atlassian.plugin.remotable.host.common.descriptor.DescriptorAccessor;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.container.internal.Environment;
import com.atlassian.plugin.remotable.container.service.ContainerOAuthSignedRequestHandler;
import com.atlassian.plugin.remotable.host.common.service.AbstractOauthSignedRequestHandler;
import org.apache.commons.io.IOUtils;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * Registers calling applications only if the secret matches.  Uses a mutable environment
 * to store values.
 */
public class RegistrationFilter implements Filter
{
    private final DescriptorAccessor descriptorAccessor;
    private final Environment environment;
    private final ContainerOAuthSignedRequestHandler requestHandler;
    private static final Logger log = LoggerFactory.getLogger(RegistrationFilter.class);

    public RegistrationFilter(DescriptorAccessor descriptor, Environment environment, SignedRequestHandler requestHandler)
    {
        this.descriptorAccessor = descriptor;
        this.environment = environment;
        this.requestHandler = (ContainerOAuthSignedRequestHandler) requestHandler;
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

        if ("post".equalsIgnoreCase(((HttpServletRequest) request).getMethod()))
        {
            if (Boolean.parseBoolean(environment.getOptionalEnv("ALLOW_REGISTRATION", "true")))
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(IOUtils.toString(req.getReader()));
                    String baseUrl = jsonObject.getString("baseUrl");
                    String productType = jsonObject.getString("productType");
                    String publicKey = jsonObject.getString("publicKey");

                    RegistrationSignedRequestHandler tmpHandler = new RegistrationSignedRequestHandler(publicKey, baseUrl,
                            requestHandler.getLocalBaseUrl());
                    String clientKey = tmpHandler.validateRequest(req);

                    log.info("Registering host - key: '{}' baseUrl: '{}'", clientKey, baseUrl);
                    requestHandler.addHost(clientKey, publicKey, baseUrl, productType);
                    resp.setStatus(204);
                    resp.getWriter().close();
                }
                catch (JSONException e)
                {
                    resp.sendError(400, "Unable to parse json body: " + e.toString());
                }
            }
            else
            {
                resp.sendError(403, "Registration not allowed");
            }
        }
        else
        {
            resp.setContentType("text/xml");
            byte[] descriptor = readDescriptorToBytes();
            resp.setContentLength(descriptor.length);
            resp.getOutputStream().write(descriptor);
            resp.getOutputStream().close();
        }
    }

    @Override
    public void destroy()
    {
    }

    private static class RegistrationSignedRequestHandler extends AbstractOauthSignedRequestHandler
    {
        private final String publicKey;
        private final String baseUrl;
        private final String localBaseUrl;

        private RegistrationSignedRequestHandler(String publicKey, String baseUrl, String localBaseUrl)
        {
            this.publicKey = publicKey;
            this.baseUrl = baseUrl;
            this.localBaseUrl = localBaseUrl;
        }

        @Override
        protected String getAuthorizationHeaderValue(URI uri, String method, String username)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        protected String getLocalBaseUrl()
        {
            return localBaseUrl;
        }

        @Override
        protected Object getHostOauthPublicKey(String key)
        {
            return publicKey;
        }

        @Override
        protected String getHostBaseUrl(String key)
        {
            return baseUrl;
        }
    }
}
