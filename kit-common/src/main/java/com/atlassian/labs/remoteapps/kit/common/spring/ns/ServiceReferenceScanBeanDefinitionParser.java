package com.atlassian.labs.remoteapps.kit.common.spring.ns;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.osgi.service.importer.support.Cardinality;
import org.springframework.osgi.service.importer.support.OsgiServiceProxyFactoryBean;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Finds any instances of @ServiceReference in constructor parameters and creates an osgi service reference bean
 * in the context using first any name defined in the annotation, then the class/interface name as the bean name.
 */
public class ServiceReferenceScanBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element element, ParserContext parserContext) {

        final BeanDefinitionRegistry registry = parserContext.getRegistry();
        for (String beanName : registry.getBeanDefinitionNames())
        {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            Class clazz;
            try
            {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(beanDef.getBeanClassName());
            }
            catch (ClassNotFoundException e)
            {
                // can't find class, ignoring
                continue;
            }

            for (Constructor constructor : clazz.getConstructors())
            {
                scanAnnotations(constructor.getParameterTypes(), constructor.getParameterAnnotations(), registry, parserContext,  element);
            }

            for (Method method : clazz.getMethods())
            {
                if (method.getName().startsWith("set") && method.getParameterTypes().length == 1)
                {
                    scanAnnotations(method.getParameterTypes(), method.getParameterAnnotations(), registry, parserContext,  element);
                }
            }
        }

		return null;
	}

    private void scanAnnotations(Class[] types, Annotation[][] annotations, BeanDefinitionRegistry registry, ParserContext context, Element element)
    {
        for (int x=0; x<annotations.length; x++)
        {
            for (Annotation paramAnn : annotations[x])
            {
                if (paramAnn instanceof ServiceReference)
                {
                    ServiceReference serviceRefAnn = (ServiceReference)paramAnn;
                    final Class paramClass = types[x];
                    String name = "".equals(serviceRefAnn.value()) ? paramClass.getSimpleName() : serviceRefAnn.value();
                    if (!registry.containsBeanDefinition(name))
                    {
                        registry.registerBeanDefinition(name, generateServiceReferenceBeanDefinition(context, paramClass, element));
                    }
                }
            }
        }
    }

    private BeanDefinition generateServiceReferenceBeanDefinition(ParserContext parserContext, Class paramClass, Element element)
    {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        Class beanClass = OsgiServiceProxyFactoryBean.class;

        if (beanClass != null) {
            builder.getRawBeanDefinition().setBeanClass(beanClass);
        }
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        builder.addPropertyValue("cardinality", Cardinality.C_1__1);
        builder.addPropertyValue("timeout", 300000);
        builder.addPropertyValue("interfaces", paramClass.getName());
        return builder.getBeanDefinition();
    }

}
