package com.atlassian.plugin.remotable.host.common.service;

import com.atlassian.plugin.remotable.api.service.RenderContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipe;
import com.atlassian.plugin.remotable.host.common.service.http.DefaultRequestContext;
import com.atlassian.plugin.remotable.host.common.service.http.bigpipe.BigPipeServiceFactory;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class RenderContextServiceFactory implements TypedServiceFactory<RenderContext>
{
    private RequestContextServiceFactory requestContextServiceFactory;
    private SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory;
    private final LocaleResolver localeResolver;
    private final I18nResolver i18nResolver;
    private final BigPipeServiceFactory bigPipeServiceFactory;

    public RenderContextServiceFactory(RequestContextServiceFactory requestContextServiceFactory,
                                       SignedRequestHandlerServiceFactory signedRequestHandlerServiceFactory,
                                       LocaleResolver localeResolver,
                                       I18nResolver i18nResolver,
                                       BigPipeServiceFactory bigPipeServiceFactory)
    {
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.signedRequestHandlerServiceFactory = signedRequestHandlerServiceFactory;
        this.localeResolver = localeResolver;
        this.i18nResolver = i18nResolver;
        this.bigPipeServiceFactory = bigPipeServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    public DefaultRenderContext getService(Bundle bundle)
    {
        DefaultRequestContext requestContext = requestContextServiceFactory.getService(bundle);
        SignedRequestHandler signedRequestHandler = signedRequestHandlerServiceFactory.getService(bundle);
        BigPipe bigPipe = bigPipeServiceFactory.getService(bundle);
        return new DefaultRenderContext(requestContext, signedRequestHandler, localeResolver, i18nResolver, bigPipe);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
