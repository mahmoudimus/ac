package com.atlassian.plugin.remotable.kit.common.spring.ns;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Finds any instances of {@link ComponentImport} in constructor parameters and creates an osgi service reference bean
 * in the context using first any name defined in the annotation, then the class/interface name as the bean name.
 */
public final class ComponentImportScanBeanDefinitionParser implements BeanDefinitionParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<? extends Annotation> annotationType = ComponentImport.class;

    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        final BeanDefinitionRegistry registry = parserContext.getRegistry();
        for (String beanName : registry.getBeanDefinitionNames())
        {
            logger.debug("Scanning bean named '{}' for constructor and setters with parameters annotated with {}", beanName, annotationType.getName());

            final BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            final Option<Class<?>> clazzOption = getClass(beanName, beanDef.getBeanClassName());
            if (!clazzOption.isDefined())
            {
                continue; // can't find class, ignoring
            }

            final Class<?> clazz = clazzOption.get();
            for (Constructor constructor : clazz.getConstructors())
            {
                scanAnnotations(constructor.getParameterTypes(), constructor.getParameterAnnotations(), registry, parserContext, element);
            }

            for (Method method : clazz.getMethods())
            {
                if (method.getName().startsWith("set") && method.getParameterTypes().length == 1)
                {
                    scanAnnotations(method.getParameterTypes(), method.getParameterAnnotations(), registry, parserContext, element);
                }
            }

            for (Class c : concat(newArrayList(clazzOption), (List<Class>) ClassUtils.getAllSuperclasses(clazz)))
            {
                for (Field field : c.getDeclaredFields())
                {
                    scanAnnotations(field.getType(), field.getDeclaredAnnotations(), registry, parserContext, element);
                }
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

    private void scanAnnotations(Class[] types, Annotation[][] annotations, BeanDefinitionRegistry registry, ParserContext context, Element element)
    {
        for (int x = 0; x < annotations.length; x++)
        {
            scanAnnotations(types[x], annotations[x], registry, context, element);
        }
    }

    private void scanAnnotations(Class clazz, Annotation[] annotations, BeanDefinitionRegistry registry, ParserContext context, Element element)
    {
        for (Annotation paramAnn : annotations)
        {
            if (paramAnn.annotationType().equals(annotationType))
            {
                logger.debug("Found parameter of type '{}' with annotation '{}'", clazz.getName(), annotationType.getName());
                final String name = clazz.getSimpleName();
                if (!registry.containsBeanDefinition(name))
                {
                    logger.debug("Registry doesn't contain bean definition '{}', adding now.", name);
                    registry.registerBeanDefinition(name, generateServiceReferenceBeanDefinition(context, clazz, element));
                }
                else
                {
                    logger.debug("Registry already contains bean definition for '{}', skipping.", name);
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
