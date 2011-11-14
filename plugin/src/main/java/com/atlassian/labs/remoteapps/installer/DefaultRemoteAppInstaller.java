package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.util.zip.ZipHandler;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.*;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Component
public class DefaultRemoteAppInstaller implements RemoteAppInstaller
{
    private final ConsumerService consumerService;
    private final RequestFactory requestFactory;
    private final PluginController pluginController;
    private final ApplicationProperties applicationProperties;

    private static final Set<String> ALLOWED_MODULES = ImmutableSet.of("plugin-info", "remote-app");

    @Autowired
    public DefaultRemoteAppInstaller(ConsumerService consumerService, RequestFactory requestFactory, PluginController pluginController, ApplicationProperties applicationProperties)
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void install(final String registrationUrl, String registrationSecret)
    {
        final URI registrationUri = URI.create(registrationUrl);
        Request request = requestFactory.createRequest(Request.MethodType.POST, registrationUrl);
        Consumer consumer = consumerService.getConsumer();

        request.addRequestParameters("token", registrationSecret, "key", consumer.getKey(), "publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()), "description", consumer.getDescription(), "requestTokenUrl", applicationProperties.getBaseUrl() + "/plugins/servlet/oauth/request-token", "accessTokenUrl", applicationProperties.getBaseUrl() + "/plugins/servlet/oauth/access-token", "authorizeUrl", applicationProperties.getBaseUrl() + "/plugins/servlet/oauth/authorize");
        try
        {
            request.execute(new ResponseHandler() {
                @Override
                public void handle(Response response) throws ResponseException
                {
                    String pluginXml = response.getResponseBodyAsString();
                    validatePluginXml(registrationUrl, pluginXml);
                    JarPluginArtifact jar = createJarPluginArtifact(registrationUri.getHost(), pluginXml);

                    // todo: validate remote app url,
                    pluginController.installPlugins(jar);
                }
            });
        }
        catch (ResponseException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void validatePluginXml(String registrationUrl, String pluginXml)
    {
        Document doc = null;
        try
        {
            doc = new SAXReader().read(new StringReader(pluginXml));
        }
        catch (DocumentException e)
        {
            throw new PluginParseException("Invalid plugin xml: \n" + pluginXml, e);
        }
        Element root = doc.getRootElement();
        if (!"2".equals(root.attributeValue("plugins-version")))
        {
            throw new PluginParseException("Plugins version must be 2");
        }
        for (Element e : ((List<Element>)root.elements()))
        {
            if (!ALLOWED_MODULES.contains(e.getName()))
            {
                throw new PluginParseException("Illegal module: " + e.getName());
            }
        }

        if (root.element("remote-app").attribute("rpc-url") != null)
        {
            throw new PluginParseException("rpc-url not allowed");
        }

        String displayUrl = root.element("remote-app").attributeValue("display-url");
        if (displayUrl == null || !registrationUrl.startsWith(displayUrl))
        {
            throw new PluginParseException("Invalid display-url '" + displayUrl + "'");
        }
    }

    private static JarPluginArtifact createJarPluginArtifact(String host, final String pluginXml)
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + host, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", pluginXml);
            }
        }));
    }
}
