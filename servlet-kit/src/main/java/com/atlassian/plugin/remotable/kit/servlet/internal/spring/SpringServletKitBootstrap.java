package com.atlassian.plugin.remotable.kit.servlet.internal.spring;

import com.atlassian.plugin.remotable.api.annotation.ServiceReference;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.kit.servlet.AbstractServletKitBootstrap;
import com.atlassian.plugin.remotable.kit.servlet.internal.MultipageServlet;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Map;

import static com.atlassian.plugin.remotable.spi.util.Strings.decapitalize;
import static com.atlassian.plugin.remotable.spi.util.Strings.removeSuffix;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 *
 */
@Singleton
public class SpringServletKitBootstrap extends AbstractServletKitBootstrap
{

    @Inject
    public SpringServletKitBootstrap(ApplicationContext applicationContext,
                                     @ServiceReference HttpResourceMounter httpResourceMounter,
                                     @ServiceReference PluginRetrievalService pluginRetrievalService,
                                     @ServiceReference SignedRequestHandler signedRequestHandler
    )
        throws Exception
    {
        Map<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> providers = newLinkedHashMap();

        for (HttpServlet servlet : (Collection<HttpServlet>) applicationContext.getBeansOfType(Servlet.class).values())
        {
                providers.put(servlet.getClass(), newProvider(servlet));
        }
        register(httpResourceMounter, pluginRetrievalService, signedRequestHandler, providers);
    }

    private static <T extends HttpServlet> Provider<T> newProvider(final T servlet)
    {
        return new Provider<T>()
        {
            @Override
            public T get()
            {
                return servlet;
            }
        };
    }

}
