package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.AbstractStringTransformedDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import com.atlassian.sal.api.ApplicationProperties;
import org.dom4j.Element;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 21/02/12 Time: 2:12 AM To change this template use
 * File | Settings | File Templates.
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
