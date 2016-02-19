package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.bonnie.search.Extractor;
import com.atlassian.confluence.api.service.content.ContentPropertyService;
import com.atlassian.confluence.plugin.descriptor.ExtractorModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

public class ContentPropertyExtractorModuleDescriptor extends ExtractorModuleDescriptor
{
    private final ContentPropertyService contentPropertyService;
    private final String contentPropertyKey;

    public ContentPropertyExtractorModuleDescriptor(
            ModuleFactory moduleFactory,
            ContentPropertyService contentPropertyService,
            String contentPropertyKey)
    {
        super(moduleFactory);

        this.contentPropertyService = contentPropertyService;
        this.contentPropertyKey = contentPropertyKey;
    }

    @Override
    public Extractor createModule()
    {
        return new ContentPropertyExtractor(contentPropertyService, contentPropertyKey);
    }
}
