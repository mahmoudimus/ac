package com.atlassian.labs.remoteapps.plugin.integration.smoketest;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.atlassian.sal.api.ApplicationProperties;
import org.dom4j.Element;

/**
 * Replaces the base url in a resource
 */
public class BaseUrlTransformer implements WebResourceTransformer
{
    private final ApplicationProperties applicationProperties;

    public BaseUrlTransformer(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public DownloadableResource transform(Element configElement, ResourceLocation location,
            String filePath, DownloadableResource nextResource)
    {
        return new AbstractStringTransformedDownloadableResource(nextResource)
        {
            @Override
            protected String transform(String originalContent)
            {
                return originalContent.replace("%%BASEURL%%", applicationProperties.getBaseUrl());
            }
        };
    }
}
