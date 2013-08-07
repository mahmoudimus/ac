package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public final class LocalEmailSenderServiceFactory implements ServiceFactory
{
    private final ProductAccessor productAccessor;
    private final PermissionManager permissionManager;
    private final UserManager userManager;

    public LocalEmailSenderServiceFactory(ProductAccessor productAccessor,
            PermissionManager permissionManager, UserManager userManager)
    {
        this.productAccessor = productAccessor;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration)
    {
        String pluginKey = OsgiHeaderUtil.getPluginKey(bundle);
        return getService(pluginKey);
    }

    public LocalEmailSender getService(String pluginKey)
    {
        return new LocalEmailSender(pluginKey, productAccessor, permissionManager, userManager);
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service)
    {
    }
}
