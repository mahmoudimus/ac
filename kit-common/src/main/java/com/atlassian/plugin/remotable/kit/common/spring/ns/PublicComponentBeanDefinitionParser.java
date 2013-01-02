package com.atlassian.plugin.remotable.kit.common.spring.ns;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.service.exporter.support.OsgiServiceFactoryBean;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Finds any instances of {@link com.atlassian.plugin.remotable.api.annotation.ComponentImport} in constructor parameters and creates an osgi service reference bean
 * in the context using first any name defined in the annotation, then the class/interface name as the bean name.
 */
public final class PublicComponentBeanDefinitionParser implements BeanDefinitionParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<? extends Annotation> annotationType = PublicComponent.class;

    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        final BeanDefinitionRegistry registry = parserContext.getRegistry();
        for (String beanName : registry.getBeanDefinitionNames())
        {
            logger.debug("Scanning bean named '{}' for the public component annotation {}", beanName, annotationType.getName());

            final BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            final Option<Class<?>> clazzOption = getClass(beanName, beanDef.getBeanClassName());
            if (!clazzOption.isDefined())
            {
                continue; // can't find class, ignoring
            }

            final Class<?> clazz = clazzOption.get();

            PublicComponent publicComponent = clazz.getAnnotation(PublicComponent.class);
            if (publicComponent != null)
            {
                registry.registerBeanDefinition(beanName + "_service", generateServiceBeanDefinition(parserContext, beanName, clazz, element));
            }
        }
        return null;
    }

    private Option<Class<?>> getClass(String beanName, String beanClassName)
    {
        try
        {
            return Option.<Class<?>>some(Thread.currentThread().getContextClassLoader().loadClass(beanClassName));
        }
        catch (ClassNotFoundException e)
        {
            logger.debug("Could not load class named '{}' for bean named '{}'", beanClassName, beanName);
            logger.debug("Here is the exception we got.", e);
            return Option.none();
        }
    }

    private BeanDefinition generateServiceBeanDefinition(ParserContext parserContext, String beanName, Class paramClass, Element element)
    {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        Class beanClass = OsgiServiceFactoryBean.class;

        if (beanClass != null) {
            builder.getRawBeanDefinition().setBeanClass(beanClass);
        }
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        builder.addPropertyValue("targetBeanName", beanName);
        builder.addPropertyValue("interfaces", paramClass.getName());
        return builder.getBeanDefinition();
    }

}
