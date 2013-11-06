package com.atlassian.plugin.spring.scanner.extension;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.w3c.dom.Element;

/**
 * This class is responsible for handling the "parsing" of the scan-indexes element in the spring beans file.
 * Ultimately, this is what kicks off the index scanner and is the starting point for registering bean definitions
 */
public class AtlassianScannerBeanDefinitionParser implements BeanDefinitionParser
{
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        // Actually scan for bean definitions and register them.
        ClassIndexBeanDefinitionScanner scanner = new ClassIndexBeanDefinitionScanner(parserContext.getReaderContext().getRegistry());
        Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan();
        
        registerComponents(parserContext.getReaderContext(), beanDefinitions, element);

        return null;
    }

    /**
     * Takes the scanned bean definitions and adds them to a root copmonent.
     * Also adds in the post-processors required to import/export OSGi services.
     * Finally fires the component registered event with our root component.
     * @param readerContext
     * @param beanDefinitions
     * @param element
     */
    protected void registerComponents(XmlReaderContext readerContext, Set<BeanDefinitionHolder> beanDefinitions, Element element)
    {
        Object source = readerContext.extractSource(element);
        CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);

        //Add the beans we found to our root component
        for (Iterator it = beanDefinitions.iterator(); it.hasNext(); )
        {
            BeanDefinitionHolder beanDefHolder = (BeanDefinitionHolder) it.next();
            compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
        }

        //add our custom post-processors along with the standard @Inject and @Autowired processors
        Set<BeanDefinitionHolder> processorDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        processorDefinitions.addAll(AnnotationConfigUtils.registerAnnotationConfigProcessors(readerContext.getRegistry(), source));
        processorDefinitions.add(getComponentImportPostProcessor(readerContext.getRegistry(), source));
        processorDefinitions.add(getServiceExportPostProcessor(readerContext.getRegistry(), source));

        for (BeanDefinitionHolder processorDefinition : processorDefinitions)
        {
            compositeDef.addNestedComponent(new BeanComponentDefinition(processorDefinition));
        }

        readerContext.fireComponentRegistered(compositeDef);
    }

    /**
     * Helper to convert a post-processor into a proper holder
     * @param registry
     * @param definition
     * @param beanName
     * @return
     */
    private BeanDefinitionHolder registerBeanPostProcessor(BeanDefinitionRegistry registry, RootBeanDefinition definition, String beanName)
    {
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        registry.registerBeanDefinition(beanName, definition);
        return new BeanDefinitionHolder(definition, beanName);
    }

    private BeanDefinitionHolder getComponentImportPostProcessor(BeanDefinitionRegistry registry, Object source)
    {
        RootBeanDefinition def = new RootBeanDefinition(ComponentImportBeanFactoryPostProcessor.class);
        def.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        def.setSource(source);
        
        return registerBeanPostProcessor(registry, def, "componentImportBeanFactoryPostProcessor");
    }

    private BeanDefinitionHolder getServiceExportPostProcessor(BeanDefinitionRegistry registry, Object source)
    {
        RootBeanDefinition def = new RootBeanDefinition(ServiceExporterBeanPostProcessor.class);
        def.setSource(source);
        def.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        return registerBeanPostProcessor(registry, def, "serviceExportBeanPostProcessor");
    }
}
