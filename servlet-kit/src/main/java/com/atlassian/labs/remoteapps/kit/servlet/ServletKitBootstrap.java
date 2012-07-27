package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.TransformingRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.PolygotRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.apputils.Environment;
import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import com.atlassian.labs.remoteapps.apputils.kit.RegistrationFilter;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.sal.api.ApplicationProperties;
import net.oauth.signature.RSA_SHA1;
import org.dom4j.Document;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Locale;

/**
 *
 */
public class ServletKitBootstrap
{
    private final ApplicationContext applicationContext;
    private final BundleContext bundleContext;
    private final OAuthContext oAuthContext;
    private static final Logger log = LoggerFactory.getLogger(ServletKitBootstrap.class);

    public ServletKitBootstrap(ApplicationContext applicationContext,
            BundleContext bundleContext,
            OAuthContext oAuthContext,
            Environment environment,
            DescriptorGenerator descriptorGenerator) throws Exception
    {
        this.applicationContext = applicationContext;
        this.bundleContext = bundleContext;
        this.oAuthContext = oAuthContext;

        String displayUrl = descriptorGenerator.getLocalMountBaseUrl();
        oAuthContext.setLocalBaseUrlIfNull(displayUrl);

        for (HttpServlet servlet : (Collection<HttpServlet>)applicationContext.getBeansOfType(Servlet.class).values())
        {
            String path;
            AppUrl appUrl = servlet.getClass().getAnnotation(AppUrl.class);
            if (appUrl != null)
            {
                path = appUrl.value();
            }
            else
            {
                String className = servlet.getClass().getSimpleName();
                path = "/" + String.valueOf(className.charAt(0)).toLowerCase(Locale.US) +
                        (className.endsWith("Servlet") ? className.substring(1, className.length() - "Servlet".length()) : className.substring(1, className.length()));
            }
            log.info("Found servlet '" + path + "' class '" + servlet.getClass());
            descriptorGenerator.mountServlet(servlet, path, path + "/*");
        }
        descriptorGenerator.mountStaticResources("/public", "/");

        RemoteAppDescriptorAccessor descriptorAccessor = getDescriptorAccessor(oAuthContext);

        if (isLocal())
        {
            registerOAuthHostInfo();
        }
        // todo: handle exceptions better
        descriptorGenerator.mountFilter(new RegistrationFilter(descriptorAccessor, environment,
                oAuthContext), "/");
        descriptorGenerator.init(descriptorAccessor.getDescriptor());
    }

    private RemoteAppDescriptorAccessor getDescriptorAccessor(final OAuthContext oAuthContext)
    {
        RemoteAppDescriptorAccessor descriptorAccessor = loadOptionalBean(RemoteAppDescriptorAccessor.class);
        if (descriptorAccessor == null)
        {
            descriptorAccessor = new PolygotRemoteAppDescriptorAccessor(bundleContext.getBundle());
        }

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
