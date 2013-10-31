package com.atlassian.plugin.connect.plugin.spring;

import java.io.IOException;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

public class ProductSpecificExclusionFilter implements TypeFilter
{
    private boolean jira;
    private boolean confluence;
    
    public ProductSpecificExclusionFilter()
    {
        try
        {
            Class.forName("com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor");
            this.jira = true;
        }
        catch (ClassNotFoundException e)
        {
            this.jira = false;
        }

        try
        {
            Class.forName("com.atlassian.confluence.plugin.descriptor.MacroModuleDescriptor");
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
        
        if(isJira())
        {
            return metadata.hasAnnotation(ConfluenceComponent.class.getName());
        }
        else if(isConfluence())
        {
            return metadata.hasAnnotation(JiraComponent.class.getName());
        }
        
        return false;
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
