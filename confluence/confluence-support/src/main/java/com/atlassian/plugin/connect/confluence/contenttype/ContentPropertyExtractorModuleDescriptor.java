package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.bonnie.search.Extractor;
import com.atlassian.confluence.api.service.content.ContentPropertyService;
import com.atlassian.confluence.plugin.descriptor.ExtractorModuleDescriptor;
import com.atlassian.plugin.connect.confluence.ConfluenceFeatureManager;
import com.atlassian.plugin.module.ModuleFactory;

public class ContentPropertyExtractorModuleDescriptor extends ExtractorModuleDescriptor {
    private final ConfluenceFeatureManager confluenceFeatureManager;
    private final ContentPropertyService contentPropertyService;
    private final String contentPropertyKey;

    public ContentPropertyExtractorModuleDescriptor(
            ModuleFactory moduleFactory,
            ConfluenceFeatureManager confluenceFeatureManager,
            ContentPropertyService contentPropertyService,
            String contentPropertyKey) {
        super(moduleFactory);

        this.confluenceFeatureManager = confluenceFeatureManager;
        this.contentPropertyService = contentPropertyService;
        this.contentPropertyKey = contentPropertyKey;
    }

    @Override
    public Extractor createModule() {
        return new ContentPropertyExtractor(confluenceFeatureManager, contentPropertyService, contentPropertyKey);
    }
}
