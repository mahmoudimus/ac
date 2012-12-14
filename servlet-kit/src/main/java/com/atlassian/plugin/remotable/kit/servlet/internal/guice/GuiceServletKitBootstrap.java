package com.atlassian.plugin.remotable.kit.servlet.internal.guice;

import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.HttpResourceMounter;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.kit.servlet.AbstractServletKitBootstrap;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.eclipse.sisu.EagerSingleton;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
import java.util.Map;

import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 *
 */
@Named
@EagerSingleton
public class GuiceServletKitBootstrap extends AbstractServletKitBootstrap
{
    @Inject
        public GuiceServletKitBootstrap(
            Injector injector,
            @ComponentImport HttpResourceMounter httpResourceMounter,
            @ComponentImport PluginRetrievalService pluginRetrievalService,
            @ComponentImport SignedRequestHandler signedRequestHandler
        )
            throws Exception
        {
            Map<Class<? extends HttpServlet>, Provider<? extends HttpServlet>> servlets = newLinkedHashMap();

            for (Map.Entry<Key<?>, Binding<?>> entry : injector.getBindings().entrySet())
            {
                if (HttpServlet.class.isAssignableFrom(entry.getKey().getTypeLiteral().getRawType()))
                {
                    servlets.put(
                            (Class<HttpServlet>) entry.getKey().getTypeLiteral().getRawType(),
                            (Provider<HttpServlet>) entry.getValue().getProvider());
                }
            }
            register(httpResourceMounter, pluginRetrievalService, signedRequestHandler, servlets);
        }
}
