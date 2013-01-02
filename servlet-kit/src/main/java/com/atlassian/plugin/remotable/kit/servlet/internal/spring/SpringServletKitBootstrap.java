package com.atlassian.plugin.remotable.kit.servlet.internal.spring;

import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.kit.servlet.AbstractServletKitBootstrap;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 *
 */
@Named
public class SpringServletKitBootstrap extends AbstractServletKitBootstrap
{
    @Inject
    public SpringServletKitBootstrap(ApplicationContext applicationContext,
                                     @ComponentImport HttpResourceMounter httpResourceMounter,
                                     @ComponentImport PluginRetrievalService pluginRetrievalService,
                                     @ComponentImport SignedRequestHandler signedRequestHandler
    )
        throws Exception
    {
        Map<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> providers = newLinkedHashMap();

        for (HttpServlet servlet : (Collection<HttpServlet>) applicationContext.getBeansOfType(Servlet.class).values())
        {
                providers.put(servlet.getClass(), newProvider(servlet));
        }
        register(httpResourceMounter, pluginRetrievalService.getPlugin(), signedRequestHandler, providers);
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
