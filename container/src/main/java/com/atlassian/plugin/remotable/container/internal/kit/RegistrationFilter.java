package com.atlassian.plugin.remotable.container.internal.kit;

import com.atlassian.plugin.remotable.descriptor.DescriptorAccessor;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.container.internal.Environment;
import com.atlassian.plugin.remotable.container.service.ContainerOAuthSignedRequestHandler;
import com.atlassian.plugin.remotable.host.common.descriptor.DocumentationUrlRedirect;
import com.atlassian.plugin.remotable.host.common.service.AbstractOauthSignedRequestHandler;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Registers calling applications only if the secret matches. Uses a mutable environment
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
        final String method = ((HttpServletRequest) request).getMethod();

        if ("post".equalsIgnoreCase(method))
        {
            JSONObject jsonObject = (JSONObject) JSONValue.parse(IOUtils.toString(req.getReader()));
            String baseUrl = (String) jsonObject.get("baseUrl");
            String productType = (String) jsonObject.get("productType");
            String publicKey = (String) jsonObject.get("publicKey");

            RegistrationSignedRequestHandler tmpHandler = new RegistrationSignedRequestHandler(publicKey, baseUrl,
                    requestHandler.getLocalBaseUrl());
            String clientKey = tmpHandler.validateRequest(req);

            if (Boolean.parseBoolean(environment.getOptionalEnv("ALLOW_REGISTRATION", "true")))
            {
                log.info("Registering host - key: '{}' baseUrl: '{}'", clientKey, baseUrl);
                requestHandler.addHost(clientKey, publicKey, baseUrl, productType);

            } else {
                if (!requestHandler.isHostRegistered(clientKey)) {
                    log.info("Invalid registration attempt for key {} at {} by {}",
                            new Object[]{clientKey, baseUrl, request.getRemoteAddr()});
                    resp.sendError(403, "Registration not allowed");
                    return;
                } else {
                    log.debug("Ignore re-registering of known host " + clientKey);
                }
            }
            resp.setStatus(204);
            resp.getWriter().close();
        }
        else if ("get".equalsIgnoreCase(method))
        {
            if (req.getHeader("Accept").startsWith("application/xml"))
            {
                serveXmlDescriptor(resp);
            }
            else
            {
                Document doc = descriptorAccessor.getDescriptor();
                Element pluginInfo = doc.getRootElement().element("plugin-info");
                if (pluginInfo != null)
                {
                    Map<String, String> params = newHashMap();
                    for (Element param : (List<Element>) pluginInfo.elements("param"))
                    {
                        params.put(param.attributeValue("name"), param.getTextTrim());
                    }
                    if (DocumentationUrlRedirect.redirect(params, resp))
                    {
                        return;
                    }
                }
                serveXmlDescriptor(resp);
            }
        }
    }

    private void serveXmlDescriptor(HttpServletResponse resp) throws IOException
    {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/xml");
        byte[] descriptor = readDescriptorToBytes();
        resp.setContentLength(descriptor.length);
        resp.getOutputStream().write(descriptor);
        resp.getOutputStream().close();
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
