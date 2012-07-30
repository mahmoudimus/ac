package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.DescriptorGenerator;
import com.atlassian.labs.remoteapps.api.PolygotRemoteAppDescriptorAccessor;
import com.atlassian.labs.remoteapps.api.RemoteAppDescriptorAccessor;
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
    private static final Logger log = LoggerFactory.getLogger(ServletKitBootstrap.class);

    public ServletKitBootstrap(ApplicationContext applicationContext,
            BundleContext bundleContext,
            DescriptorGenerator descriptorGenerator) throws Exception
    {
        this.applicationContext = applicationContext;
        this.bundleContext = bundleContext;

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

        RemoteAppDescriptorAccessor descriptorAccessor = getDescriptorAccessor();

        // todo: handle exceptions better
        descriptorGenerator.init(descriptorAccessor);
    }

    private RemoteAppDescriptorAccessor getDescriptorAccessor()
    {
        RemoteAppDescriptorAccessor descriptorAccessor = loadOptionalBean(RemoteAppDescriptorAccessor.class);
        if (descriptorAccessor == null)
        {
            descriptorAccessor = new PolygotRemoteAppDescriptorAccessor(bundleContext.getBundle());
        }

        return descriptorAccessor;
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
