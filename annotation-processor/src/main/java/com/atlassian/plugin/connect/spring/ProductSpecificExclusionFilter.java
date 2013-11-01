package com.atlassian.plugin.connect.spring;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.plugin.connect.annotation.ProductFilter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import static com.google.common.collect.Lists.newArrayList;

public class ProductSpecificExclusionFilter implements TypeFilter
{
	public static final String PRODUCTS = "value";
    @VisibleForTesting
    static final String CLASS_ON_JIRA_CLASSPATH = "com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor";
    @VisibleForTesting
    static final String CLASS_ON_CONFLUENCE_CLASSPATH = "com.atlassian.confluence.plugin.descriptor.MacroModuleDescriptor";

    private boolean jira;
    private boolean confluence;
    private ProductFilter filterForProduct;
    
    public ProductSpecificExclusionFilter()
    {
        try
        {
            Class.forName(CLASS_ON_JIRA_CLASSPATH);
            filterForProduct = ProductFilter.JIRA;
        }
        catch (ClassNotFoundException e)
        {
        }

        try
        {
            Class.forName(CLASS_ON_CONFLUENCE_CLASSPATH);
            filterForProduct = ProductFilter.CONFLUENCE;
        }
        catch (ClassNotFoundException e)
        {
        }
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException
    {
        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
        List<ProductFilter> filters = findProductFiltersFromAnnotations(metadata);
        
        if(filters.isEmpty())
        {
            return false;
        }
        
        if(!filters.contains(filterForProduct))
        {
            return true;
        }
        
        return false;
    }

    private List<ProductFilter> findProductFiltersFromAnnotations(AnnotationMetadata metadata)
    {
      
        return newArrayList(Iterables.filter(Iterables.transform(metadata.getAnnotationTypes(),new Function<String, ProductFilter>() {
            @Override
            public ProductFilter apply(@Nullable String input)
            {
                String annoName = StringUtils.substringAfterLast(input,".").toUpperCase();
                for(ProductFilter filter : ProductFilter.values())
                {
                    if(annoName.startsWith(filter.name()))
                    {
                        return filter;
                    }
                }
                
                return null;
            }
        }),new Predicate<ProductFilter>() {
            @Override
            public boolean apply(@Nullable ProductFilter input)
            {
                return null != input;
            }
        }));

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
