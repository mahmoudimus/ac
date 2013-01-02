package com.atlassian.plugin.remotable.kit.common.spring.ns;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.annotation.PublicComponent;
import com.atlassian.plugin.remotable.kit.common.spring.NamedAnnotationBeanNameGenerator;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;

import static com.google.common.collect.Iterables.toArray;

/**
 * Scans packages for beans using standard javax.inject annotations.  Only activates if Plugins 3
 * has been detected
 */
public class BeanScanBeanDefinitionParser implements BeanDefinitionParser
{
    private final Logger log = LoggerFactory.getLogger(BeanScanBeanDefinitionParser.class);

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        final Iterable<String> basePackages = getBasePackages(element);

        if (!plugins3Available())
        {
            log.debug("Plugins 3 not available, aborting scanning of packages: {}",  basePackages);
            return null;
        }
        else
        {
            log.debug("Scanning packages {} for spring components");
        }

        registerAutowireAnnotationPostProcessor(parserContext, element, "__component_import", ComponentImport.class);
        registerAutowireAnnotationPostProcessor(parserContext, element, "__inject", Inject.class);

        final ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(parserContext.getRegistry(), false);

        scanner.setBeanNameGenerator(new NamedAnnotationBeanNameGenerator());
        scanner.addIncludeFilter(new AnnotationTypeFilter(Named.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(PublicComponent.class));
        scanner.setResourceLoader(parserContext.getReaderContext().getResourceLoader());
        scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());

        final int scanned = scanner.scan(toArray(basePackages, String.class));
        log.debug("Found {} components scanning packages {}", scanned, basePackages);

        new ComponentImportScanBeanDefinitionParser().parse(element, parserContext);
        new PublicComponentBeanDefinitionParser().parse(element, parserContext);

        return null;
    }

    private Iterable<String> getBasePackages(Element element)
    {
        final ImmutableList.Builder<String> basePackages = ImmutableList.builder();
        final NodeList nl = element.getChildNodes();
        for (int x = 0; x < nl.getLength(); x++)
        {
            if (nl.item(x).getNodeType() == Node.ELEMENT_NODE)
            {
                basePackages.add(nl.item(x).getTextContent());
            }
        }
        return basePackages.build();
    }

    private void registerAutowireAnnotationPostProcessor(ParserContext parserContext,
            Element element, String beanName,
            Class<? extends Annotation> annotationClass)
    {
        if (parserContext.getRegistry().isBeanNameInUse(beanName))
        {
            log.debug("Bean '" + beanName + "' is already in use, won't register annotation scanner " +
                    "for " + annotationClass.getName());
            return;
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        builder.getRawBeanDefinition().setBeanClass(AutowiredAnnotationBeanPostProcessor.class);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        builder.addPropertyValue("autowiredAnnotationType", annotationClass);
        parserContext.getRegistry().registerBeanDefinition(beanName,
                builder.getBeanDefinition());
    }

    private boolean plugins3Available()
    {
        try
        {
            getClass().getClassLoader().loadClass("com.atlassian.plugin.remotable.api.service.HttpResourceMounter");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
