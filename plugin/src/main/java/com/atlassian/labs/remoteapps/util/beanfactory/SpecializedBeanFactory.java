package com.atlassian.labs.remoteapps.util.beanfactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Constructs beans that need stack-specific objects injected
 */
@Component
public class SpecializedBeanFactory
{
    private final ApplicationContext applicationContext;

    @Autowired
    public SpecializedBeanFactory(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    public <T> T construct(Class<T> beanClass, Map<String,Object> injectables)
    {
        DefaultListableBeanFactory specializedFactory = new DefaultListableBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        for (Map.Entry<String,Object> injectable : injectables.entrySet())
        {
            specializedFactory.registerSingleton(
                    injectable.getKey(),
                    injectable.getValue()
            );
        }

        return (T) specializedFactory.createBean(beanClass, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
    }

    public <T> T construct(Class<T> beanClass, Object... injectables)
    {
        Map<String,Object> map = newHashMap();
        for (Object injectable : injectables)
        {
            map.put(String.valueOf(System.identityHashCode(injectable)), injectable);
        }
        return construct(beanClass, map);
    }
}
