package com.atlassian.labs.remoteapps.kit.js;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.apputils.kit.RegistrationServlet;
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

        descriptorGenerator.mountServlet(servlet, "/*");
        descriptorGenerator.mountStaticResources("/public");

        Document appDescriptor = new RemoteAppDescriptorAccessor(bundleContext.getBundle()).getDescriptor();
        Element root = appDescriptor.getRootElement();
        oAuthContext.setLocalOauthKey(root.attributeValue("key"));

        updateDisplayUrl(root);
        updateOauthPublicKey(oAuthContext, root);

        if (isLocal())
        {
            registerOAuthHostInfo();
        }
        // todo: handle exceptions better
        descriptorGenerator.mountServlet(new RegistrationServlet(appDescriptor, environment,
                oAuthContext), "/register");
        descriptorGenerator.init(appDescriptor);
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
