package com.atlassian.plugin.connect.plugin.spring;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;

import com.google.common.annotations.VisibleForTesting;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

public class ProductSpecificExclusionFilter implements TypeFilter
{
	public static final String PRODUCTS = "products";
    @VisibleForTesting
    static final String CLASS_ON_JIRA_CLASSPATH = "com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor";
    @VisibleForTesting
    static final String CLASS_ON_CONFLUENCE_CLASSPATH = "com.atlassian.confluence.plugin.descriptor.MacroModuleDescriptor";

    private boolean jira;
    private boolean confluence;
    
    public ProductSpecificExclusionFilter()
    {
        try
        {
            Class.forName(CLASS_ON_JIRA_CLASSPATH);
            this.jira = true;
        }
        catch (ClassNotFoundException e)
        {
            this.jira = false;
        }

        try
        {
            Class.forName(CLASS_ON_CONFLUENCE_CLASSPATH);
            this.confluence = true;
        }
        catch (ClassNotFoundException e)
        {
            this.confluence = false;
        }
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException
    {
        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        
        boolean mismatch = false;
        
        if(isJira())
        {
            if(metadata.hasAnnotation(ConfluenceComponent.class.getName()))
            {
                mismatch = true;
            }
            else if(metadata.hasAnnotation(ScopedComponent.class.getName()))
            {
                List<ProductFilter> products = Arrays.asList((ProductFilter[]) metadata.getAnnotationAttributes(ScopedComponent.class.getName()).get(PRODUCTS));
                mismatch = (!products.contains(ProductFilter.ALL) && !products.contains(ProductFilter.JIRA));
            } 
        }
        else if(isConfluence())
        {
            if(metadata.hasAnnotation(JiraComponent.class.getName()))
            {
                mismatch = true;
            }
            else if(metadata.hasAnnotation(ScopedComponent.class.getName()))
            {
                List<ProductFilter> products = Arrays.asList((ProductFilter[]) metadata.getAnnotationAttributes(ScopedComponent.class.getName()).get(PRODUCTS));
                mismatch = (!products.contains(ProductFilter.ALL) && !products.contains(ProductFilter.CONFLUENCE));
            }
        }
        
        return mismatch;
    }

    public boolean isJira()
    {
        return jira;
    }

    public boolean isConfluence()
    {
        return confluence;
    }
}
