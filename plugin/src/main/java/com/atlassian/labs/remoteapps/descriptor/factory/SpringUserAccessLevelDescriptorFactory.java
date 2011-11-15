package com.atlassian.labs.remoteapps.descriptor.factory;

import com.atlassian.plugin.ModuleDescriptor;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SpringUserAccessLevelDescriptorFactory implements FactoryBean
{
    private final AutowireCapableBeanFactory beanFactory;

    @Autowired
    public SpringUserAccessLevelDescriptorFactory(AutowireCapableBeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
    }

    private boolean isSpeakeasyAvailable()
    {
        try
        {
            getClass().getClassLoader().loadClass("com.atlassian.labs.speakeasy.external.SpeakeasyService");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public Object getObject() throws Exception
    {
        return isSpeakeasyAvailable() ? beanFactory.createBean(SpeakeasyAccessLevelDescriptorFactory.class) :
                                        new UserAccessLevelDescriptorFactory()
                                        {
                                            @Override
                                            public ModuleDescriptor createWebItemModuleDescriptor(BundleContext targetBundleContext)
                                            {
                                                throw new UnsupportedOperationException();
                                            }
                                        };
    }

    @Override
    public Class getObjectType()
    {
        return UserAccessLevelDescriptorFactory.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
