package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.plugin.remotable.host.common.service.RequestContextServiceFactory;
import com.atlassian.plugin.remotable.host.common.service.TypedServiceFactory;
import com.atlassian.sal.api.user.UserManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class ContainerUserManagerServiceFactory implements TypedServiceFactory<UserManager>
{
    private final RequestContextServiceFactory requestContextServiceFactory;

    public ContainerUserManagerServiceFactory(RequestContextServiceFactory requestContextServiceFactory)
    {
        this.requestContextServiceFactory = requestContextServiceFactory;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        return getService(bundle);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }

    @Override
    public UserManager getService(Bundle bundle)
    {
        return new ContainerUserManager(requestContextServiceFactory.getService(bundle));
    }
}
