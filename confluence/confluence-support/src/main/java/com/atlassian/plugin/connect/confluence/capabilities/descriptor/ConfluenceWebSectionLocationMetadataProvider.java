package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.plugin.connect.spi.web.WebSectionLocationMetadataProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import java.util.Collections;
import java.util.List;

@ConfluenceComponent
public class ConfluenceWebSectionLocationMetadataProvider implements WebSectionLocationMetadataProvider
{
    @Override
    public List<String> getMovableWebSectionLocations()
    {
        return Collections.emptyList();
    }
}
