package com.atlassian.plugin.connect.plugin.spring;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

public class ProductSpecificExclusionFilter implements TypeFilter
{
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
