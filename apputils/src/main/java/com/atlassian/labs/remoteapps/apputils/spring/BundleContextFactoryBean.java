package com.atlassian.labs.remoteapps.apputils.spring;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.osgi.context.BundleContextAware;

import static com.google.common.base.Preconditions.checkNotNull;

public final class BundleContextFactoryBean implements FactoryBean, BundleContextAware
{
    private BundleContext bundleContext;

    @Override
    public Object getObject() throws Exception
    {
        return bundleContext;
    }

    @Override
    public Class getObjectType()
    {
        return BundleContext.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext);
    }
}
