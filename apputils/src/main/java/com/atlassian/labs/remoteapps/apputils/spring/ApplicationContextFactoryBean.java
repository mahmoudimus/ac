package com.atlassian.labs.remoteapps.apputils.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static com.google.common.base.Preconditions.*;

public final class ApplicationContextFactoryBean implements FactoryBean, ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public Object getObject() throws Exception
    {
        return applicationContext;
    }

    @Override
    public Class getObjectType()
    {
        return ApplicationContext.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = checkNotNull(applicationContext);
    }
}
