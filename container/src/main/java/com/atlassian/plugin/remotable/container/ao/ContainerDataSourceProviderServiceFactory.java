package com.atlassian.plugin.remotable.container.ao;

import com.atlassian.plugin.remotable.container.service.sal.ContainerApplicationPropertiesServiceFactory;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public final class ContainerDataSourceProviderServiceFactory implements ServiceFactory
{
    private ContainerDataSourceProvider containerDataSourceProvider;
    private final ContainerApplicationPropertiesServiceFactory applicationProperties;

    public ContainerDataSourceProviderServiceFactory(ContainerApplicationPropertiesServiceFactory applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
        containerDataSourceProvider = new ContainerDataSourceProvider(applicationProperties.getService(pluginKey));
        return containerDataSourceProvider;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
        containerDataSourceProvider.destroy();
    }
}
