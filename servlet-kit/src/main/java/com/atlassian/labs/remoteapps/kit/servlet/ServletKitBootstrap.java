package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.ApplicationProperties;
import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 *
 */
public class ServletKitBootstrap implements DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(ServletKitBootstrap.class);

    private final HttpServer httpServer;
    private final BundleContext bundleContext;
    private final OAuthContext oAuthContext;
    private final ApplicationContext applicationContext;
    private final ConsumerService consumerService;
    private final ApplicationProperties applicationProperties;

    public ServletKitBootstrap(
            HttpServer httpServer, BundleContext bundleContext, OAuthContext oAuthContext,
            ApplicationContext applicationContext, ConsumerService consumerService,
            ApplicationProperties applicationProperties, ApplicationProperties properties) throws Exception
    {
        this.httpServer = httpServer;
        this.bundleContext = bundleContext;
        this.oAuthContext = oAuthContext;
        this.applicationContext = applicationContext;
        this.consumerService = consumerService;
        this.applicationProperties = applicationProperties;
        register();
    }

    @Override
    public void destroy() throws Exception
    {
        httpServer.stop();
    }

    private void register() throws Exception
    {
        httpServer.start();
        try
        {
            // todo: make this work for outside the atlassian app
            Consumer consumer = consumerService.getConsumer();
            oAuthContext.setHost(consumer.getKey(),
                    RSAKeys.toPemEncoding(consumer.getPublicKey()),
                            applicationProperties.getBaseUrl());

            Document appDescriptor = loadDescriptor();
            Environment.setEnv("OAUTH_LOCAL_KEY", appDescriptor.getRootElement().attributeValue("key"));
            appDescriptor.getRootElement().addAttribute("display-url", httpServer.getAppBaseUrl().toString());
            Element oauth = appDescriptor.getRootElement().element("oauth");
            if (oauth != null)
            {
                oauth.element("public-key").setText(
                        oAuthContext.getLocal().getProperty(RSA_SHA1.PUBLIC_KEY).toString());
            }
            loadGenerator().init(appDescriptor);
        }
        catch (Exception ex)
        {
            destroy();
            throw ex;
        }
    }

    private DescriptorGenerator loadGenerator()
    {
        DescriptorGenerator generator = loadOptionalBean(DescriptorGenerator.class);
        if (generator == null)
        {
            ServiceTracker tracker = new ServiceTracker(bundleContext, 
                    DescriptorGenerator.class.getName(), null);
            tracker.open();
            try
            {
                generator = (DescriptorGenerator) tracker.waitForService(20 * 1000);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Remote app default descriptor generator not found");
            }
            finally
            {
                tracker.close();
            }
        }
        return generator;
    }

    private Document loadDescriptor()
    {
        RemoteAppDescriptorFactory factory = loadOptionalBean(RemoteAppDescriptorFactory.class);
        if (factory != null)
        {
            return factory.create();
        }

        try
        {
            return createSecureSaxReader().read(
                    bundleContext.getBundle().getEntry("atlassian-remote-app" +
                            ".xml"));
        }
        catch (DocumentException e)
        {
            throw new PluginParseException("Unable to read and parse app descriptor", e);
        }
    }
    
    <T> T loadOptionalBean(Class<T> typeClass)
    {
        Collection<T> factories = (Collection<T>) applicationContext.getBeansOfType(typeClass).values();
        if (!factories.isEmpty())
        {
            return factories.iterator().next();
        }
        return null;
    }
}
