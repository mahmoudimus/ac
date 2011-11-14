package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.speakeasy.external.SpeakeasyService;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;

/**
 *
 */
@Component
public class SpringDescriptorFactory implements FactoryBean
{
    private final AutowireCapableBeanFactory beanFactory;

    @Autowired
    public SpringDescriptorFactory(AutowireCapableBeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
    }

    private boolean isSpeakeasyAvailable()
    {
        try
        {
            getClass().getClassLoader().loadClass("com.atlassian.labs.speakeasy.external.SpeakeasyService");
            return !Boolean.getBoolean("remoteapps.speakeasy.ignore");
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public Object getObject() throws Exception
    {
        return isSpeakeasyAvailable() ? beanFactory.createBean(SpeakeasyDescriptorFactory.class) :
                                        beanFactory.createBean(GlobalDescriptorFactory.class);
    }

    @Override
    public Class getObjectType()
    {
        return DescriptorFactory.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
