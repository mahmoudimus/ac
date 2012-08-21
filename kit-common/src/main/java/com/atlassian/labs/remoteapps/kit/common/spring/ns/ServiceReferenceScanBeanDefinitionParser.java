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

                Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
                for (int x=0; x<parameterAnnotations.length; x++)
                {
                    for (Annotation paramAnn : parameterAnnotations[x])
                    {
                        if (paramAnn instanceof ServiceReference)
                        {
                            ServiceReference serviceRefAnn = (ServiceReference)paramAnn;
                            final Class paramClass = constructor.getParameterTypes()[x];
                            String name = "".equals(serviceRefAnn.value()) ? paramClass.getSimpleName()
                                    : serviceRefAnn.value();

                            if (!registry.containsBeanDefinition(name))
                            {
                                registry.registerBeanDefinition(name, generateServiceReferenceBeanDefinition(parserContext, paramClass, element));
                            }
                        }
                    }

                }
            }
        }
		return null;
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
