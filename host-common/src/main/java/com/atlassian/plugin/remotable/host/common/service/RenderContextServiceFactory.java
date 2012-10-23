package com.atlassian.plugin.remotable.host.common.service;

import com.atlassian.plugin.remotable.api.service.RenderContext;
import com.atlassian.plugin.remotable.host.common.service.http.DefaultRequestContext;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class RenderContextServiceFactory implements TypedServiceFactory<RenderContext>
{
    private RequestContextServiceFactory requestContextServiceFactory;
    private final LocaleResolver localeResolver;
    private final I18nResolver i18nResolver;

    public RenderContextServiceFactory(RequestContextServiceFactory requestContextServiceFactory,
                                       LocaleResolver localeResolver,
                                       I18nResolver i18nResolver)
    {
        this.requestContextServiceFactory = requestContextServiceFactory;
        this.localeResolver = localeResolver;
        this.i18nResolver = i18nResolver;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    public DefaultRenderContext getService(Bundle bundle)
    {
        DefaultRequestContext requestContext = requestContextServiceFactory.getService(bundle);
        return new DefaultRenderContext(requestContext, localeResolver, i18nResolver);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
