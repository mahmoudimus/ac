package com.atlassian.labs.remoteapps.kit.js;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.TransformingRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.PolygotRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.apputils.kit.RegistrationFilter;
import com.atlassian.labs.remoteapps.kit.js.ringojs.RingoEngine;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.ringojs.jsgi.JsgiServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 */
public class RingoJsKitBootstrap
{
    private final BundleContext bundleContext;
    private final OAuthContext oAuthContext;
    private static final Logger log = LoggerFactory.getLogger(RingoJsKitBootstrap.class);

    public RingoJsKitBootstrap(
            BundleContext bundleContext,
            OAuthContext oAuthContext,
            Environment environment,
            PluginRetrievalService pluginRetrievalService,
            DescriptorGenerator descriptorGenerator) throws Exception
    {
        this.bundleContext = bundleContext;
        this.oAuthContext = oAuthContext;

        log.info("Starting app '" + bundleContext.getBundle().getSymbolicName() + "'");
        String displayUrl = descriptorGenerator.getLocalMountBaseUrl();
        oAuthContext.setLocalBaseUrlIfNull(displayUrl);

        RingoEngine ringoEngine = new RingoEngine(pluginRetrievalService.getPlugin(), bundleContext);
        JsgiServlet servlet = new JsgiServlet(ringoEngine.getEngine());

        RemoteAppDescriptorAccessor descriptorAccessor = getDescriptorAccessor();
        if (isLocal())
        {
            registerOAuthHostInfo();
        }

        // this is different than servlet kit because of how we mount a single handler on /
        descriptorGenerator.mountStaticResources("/", "/public/*");

        descriptorGenerator.mountServlet(servlet, "/");

        // todo: handle exceptions better
        descriptorGenerator.mountFilter(new RegistrationFilter(descriptorAccessor, environment,
                oAuthContext), "/");

        descriptorGenerator.init(descriptorAccessor.getDescriptor());
    }

    private RemoteAppDescriptorAccessor getDescriptorAccessor()
    {
        File baseDir = new File(System.getProperty("plugin.resource.directories"));
        RemoteAppDescriptorAccessor descriptorAccessor = new PolygotRemoteAppDescriptorAccessor(
                baseDir);

        return new TransformingRemoteAppDescriptorAccessor(descriptorAccessor)
        {
            @Override
            protected Document transform(Document document)
            {
                Element root = document.getRootElement();
                oAuthContext.setLocalOauthKey(root.attributeValue("key"));

                updateDisplayUrl(root);
                updateOauthPublicKey(oAuthContext, root);
                return document;
            }
        };
    }

    private void updateOauthPublicKey(OAuthContext oAuthContext, Element root)
    {
        Element oauth = root.element("oauth");
        if (oauth != null)
        {
            Element publicKeyElement = oauth.element("public-key");
            if (publicKeyElement == null)
            {
                publicKeyElement = oauth.addElement("public-key");
            }
            publicKeyElement.setText(
                    oAuthContext.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());
        }
    }

    private void updateDisplayUrl(Element root)
    {
        root.addAttribute("display-url", oAuthContext.getLocalBaseUrl());
    }

    private boolean isLocal()
    {
        return bundleContext.getServiceReference("com.atlassian.oauth.consumer.ConsumerService") != null;
    }

    private void registerOAuthHostInfo()
    {
        ConsumerService consumerService = (ConsumerService) bundleContext.getService(
                bundleContext.getServiceReference(ConsumerService.class.getName()));
        ApplicationProperties applicationProperties = (ApplicationProperties) bundleContext.getService(
                bundleContext.getServiceReference(ApplicationProperties.class.getName()));

        Consumer consumer = consumerService.getConsumer();
        oAuthContext.addHost(consumer.getKey(),
                RSAKeys.toPemEncoding(consumer.getPublicKey()),
                applicationProperties.getBaseUrl());
    }

}
