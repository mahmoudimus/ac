package com.atlassian.labs.remoteapps.api.services;

import com.atlassian.labs.remoteapps.api.services.impl.DefaultRequestContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class RequestContextServiceFactory implements ServiceFactory
{
    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        return getService(bundle);
    }

    public RequestContext getService(Bundle bundle)
    {
        return new DefaultRequestContext();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o)
    {
    }
}
