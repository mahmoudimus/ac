package com.atlassian.plugin.connect.plugin.spring;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import com.atlassian.plugin.connect.processor.ComponentAnnotationProcessor;

import com.google.common.base.Charsets;

import org.apache.commons.lang.StringUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

public class ClassIndexBeanDefinitionScanner extends ClassPathBeanDefinitionScanner
{
    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    public ClassIndexBeanDefinitionScanner(BeanDefinitionRegistry registry)
    {
        super(registry);
    }

    public ClassIndexBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters)
    {
        super(registry, useDefaultFilters);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages)
    {
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        Set<BeanDefinition> candidates = findCandidateComponents("");
        for (BeanDefinition candidate : candidates)
        {
            String beanName = this.beanNameGenerator.generateBeanName(candidate, getRegistry());
            if (candidate instanceof AbstractBeanDefinition)
            {
                postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
            }
            ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
            if (checkCandidate(beanName, candidate))
            {
                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
                definitionHolder = applyScope(definitionHolder, scopeMetadata);
                beanDefinitions.add(definitionHolder);
                registerBeanDefinition(definitionHolder, getRegistry());
            }
        }
        return beanDefinitions;
    }

    @Override
    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator)
    {
        super.setBeanNameGenerator(beanNameGenerator);
        this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
    }

    @Override
    public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver)
    {
        super.setScopeMetadataResolver(scopeMetadataResolver);
        this.scopeMetadataResolver = scopeMetadataResolver;
    }

    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        super.setResourceLoader(resourceLoader);
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }


    @Override
    public Set<BeanDefinition> findCandidateComponents(String basePackage)
    {
        Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();

        try
        {

            Resource[] resources = readIndexFile(ComponentAnnotationProcessor.ANNOTATED_INDEX_PREFIX + Component.class.getName());
            boolean traceEnabled = logger.isTraceEnabled();
            boolean debugEnabled = logger.isDebugEnabled();
            for (int i = 0; i < resources.length; i++)
            {
                Resource resource = resources[i];
                if (traceEnabled)
                {
                    logger.trace("Scanning " + resource);
                }
                if (resource.isReadable())
                {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    if (isCandidateComponent(metadataReader))
                    {
                        ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                        sbd.setResource(resource);
                        sbd.setSource(resource);
                        if (isCandidateComponent(sbd))
                        {
                            if (debugEnabled)
                            {
                                logger.debug("Identified candidate component class: " + resource);
                            }
                            candidates.add(sbd);
                        }
                        else
                        {
                            if (debugEnabled)
                            {
                                logger.debug("Ignored because not a concrete top-level class: " + resource);
                            }
                        }
                    }
                    else
                    {
                        if (traceEnabled)
                        {
                            logger.trace("Ignored because not matching any filter: " + resource);
                        }
                    }
                }
                else
                {
                    if (traceEnabled)
                    {
                        logger.trace("Ignored because not readable: " + resource);
                    }
                }
            }
        }
        catch (IOException ex)
        {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
        return candidates;
    }

    private BeanDefinitionHolder applyScope(BeanDefinitionHolder definitionHolder, ScopeMetadata scopeMetadata)
    {
        String scope = scopeMetadata.getScopeName();
        ScopedProxyMode scopedProxyMode = scopeMetadata.getScopedProxyMode();
        definitionHolder.getBeanDefinition().setScope(scope);
        if (BeanDefinition.SCOPE_SINGLETON.equals(scope) || BeanDefinition.SCOPE_PROTOTYPE.equals(scope) ||
                scopedProxyMode.equals(ScopedProxyMode.NO))
        {
            return definitionHolder;
        }
        boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
        return ScopedProxyCreator.createScopedProxy(definitionHolder, getRegistry(), proxyTargetClass);
    }

    private static class ScopedProxyCreator
    {

        public static BeanDefinitionHolder createScopedProxy(
                BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry, boolean proxyTargetClass)
        {

            return ScopedProxyUtils.createScopedProxy(definitionHolder, registry, proxyTargetClass);
        }
    }

    private Resource[] readIndexFile(String resourceFile)
    {
        List<Resource> resources = new ArrayList<Resource>();

        try
        {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resourceFile);

            while (urls.hasMoreElements())
            {
                URL resource = urls.nextElement();
                BufferedReader reader;
                try
                {
                    reader = new BufferedReader(new InputStreamReader(resource.openStream(),
                            Charsets.UTF_8));
                }
                catch (FileNotFoundException e)
                {
                    continue;
                }

                String line = reader.readLine();
                while (line != null)
                {
                    resources.add(getResourceLoader().getResource("classpath:" + ClassUtils.convertClassNameToResourcePath(line) + ".class"));

                    line = reader.readLine();
                }

                reader.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Evo Class Index: Cannot read class index", e);
        }
        return resources.toArray(new Resource[]{});
    }
}
