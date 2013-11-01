package com.atlassian.plugin.connect.spring;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.w3c.dom.Element;

public class AtlasPluginComponentScanBeanDefinitionParser extends ComponentScanBeanDefinitionParser
{

    private static final String RESOURCE_PATTERN_ATTRIBUTE = "resource-pattern";

    private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";
    
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        // Actually scan for bean definitions and register them.
        ClassIndexBeanDefinitionScanner scanner = configureScanner(parserContext, element);
        Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan();
        registerComponents(parserContext.getReaderContext(), beanDefinitions, element);

        return null;
    }
    
    @Override
    protected ClassIndexBeanDefinitionScanner configureScanner(ParserContext parserContext, Element element) {
        XmlReaderContext readerContext = parserContext.getReaderContext();

        boolean useDefaultFilters = true;
        if (element.hasAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)) {
            useDefaultFilters = Boolean.valueOf(element.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE));
        }

        // Delegate bean definition registration to scanner class.
        ClassIndexBeanDefinitionScanner scanner = createScanner(readerContext, useDefaultFilters);
        scanner.setResourceLoader(readerContext.getResourceLoader());
        scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
        scanner.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());

        try {
            parseBeanNameGenerator(element, scanner);
        }
        catch (Exception ex) {
            readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
        }

        try {
            parseScope(element, scanner);
        }
        catch (Exception ex) {
            readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
        }

        parseTypeFilters(element, scanner, readerContext);

        return scanner;
    }

    protected ClassIndexBeanDefinitionScanner createScanner(XmlReaderContext readerContext, boolean useDefaultFilters) {
        return new ClassIndexBeanDefinitionScanner(readerContext.getRegistry(), useDefaultFilters);
    }
}
