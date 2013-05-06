package com.atlassian.plugin.remotable.container.internal.kit;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.container.Container;
import com.atlassian.plugin.remotable.container.internal.Environment;
import com.atlassian.plugin.remotable.container.service.ContainerOAuthSignedRequestHandler;
import com.atlassian.plugin.remotable.descriptor.DescriptorAccessor;
import com.atlassian.plugin.remotable.host.common.descriptor.DocumentationUrlRedirect;
import com.atlassian.plugin.remotable.host.common.service.AbstractOauthSignedRequestHandler;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.util.tracker.ServiceTracker;
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
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Registers calling applications only if the secret matches. Uses a mutable environment
 * to store values.
 */
public class RegistrationFilter implements Filter
{
    private final DescriptorAccessor descriptorAccessor;
    private final Environment environment;
    private final ServiceTracker httpClientTracker;
    private final ContainerOAuthSignedRequestHandler requestHandler;
    private static final Logger log = LoggerFactory.getLogger(RegistrationFilter.class);

    public RegistrationFilter(DescriptorAccessor descriptor, Environment environment,
            SignedRequestHandler requestHandler, ServiceTracker httpClientTracker)
    {
        this.descriptorAccessor = descriptor;
        this.environment = environment;
        this.httpClientTracker = httpClientTracker;
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
            final String baseUrl = (String) jsonObject.get("baseUrl");
            String productType = (String) jsonObject.get("productType");
            final String publicKey = (String) jsonObject.get("publicKey");

            RegistrationSignedRequestHandler tmpHandler = new RegistrationSignedRequestHandler(publicKey, baseUrl,
                    requestHandler.getLocalBaseUrl());
            final String clientKey = tmpHandler.validateRequest(req);

            if (Boolean.parseBoolean(environment.getOptionalEnv("ALLOW_REGISTRATION", "true")))
            {
                if (!Container.isDevMode() && !URI.create(baseUrl).getHost().endsWith(".jira.com"))
                {
                    resp.sendError(403, "Registrations only allowed from jira.com domains");
                    return;
                }

                final String oauthUrl = (String) ((Map)jsonObject.get("links")).get("oauth");
                if (oauthUrl.startsWith(baseUrl))
                {
                    if (!verifyOAuthFromHost(baseUrl, publicKey, clientKey, oauthUrl))
                    {
                        resp.sendError(403, "OAuth information doesn't match the base URL");
                        return;
                    }
                    log.info("Registering host - key: '{}' baseUrl: '{}'", clientKey, baseUrl);
                    requestHandler.addHost(clientKey, publicKey, baseUrl, productType);
                }
                else
                {
                    log.warn("Attempt to send oauth URL '{}' that doesn't match base URL '{}'", oauthUrl, baseUrl);
                    resp.sendError(403, "OAuth URL doesn't match the base URL");
                }
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

    private boolean verifyOAuthFromHost(final String baseUrl, final String publicKey, final String clientKey,
            final String oauthUrl)
    {
        HttpClient httpClient;
        try
        {
            httpClient = (HttpClient) httpClientTracker.waitForService(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException ignore)
        {
            return false;
        }
        return httpClient.newRequest(oauthUrl).get().<Boolean>transform()
            .ok(new Function<Response, Boolean>()
            {
                @Override
                public Boolean apply(Response input)
                {
                    String body = input.getEntity();
                    Map json = (Map) JSONValue.parse(body);
                    if (publicKey.equals(json.get("publicKey")) &&
                            clientKey.equals(json.get("key")))
                    {
                        return true;
                    }
                    else
                    {
                        log.warn("Attempt to register with public key '{}' and id '{}' that don't match the " +
                                "base URL '{}'", new Object[]{publicKey, clientKey, baseUrl});
                        return false;
                    }

                }
            })
        .otherwise(new Function<Throwable, Boolean>()
        {
            @Override
            public Boolean apply(Throwable input)
            {
                log.warn("Unexpected response when retrieving OAuth information: " + input.getMessage());
                log.debug("Problem retreiving oauth info", input);
                return false;
            }
        }).claim();
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
