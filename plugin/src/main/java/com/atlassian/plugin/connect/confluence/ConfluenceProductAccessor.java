package com.atlassian.plugin.connect.confluence;

import com.atlassian.confluence.license.LicenseService;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@ConfluenceComponent
public final class ConfluenceProductAccessor implements ProductAccessor
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceProductAccessor.class);
    private final MultiQueueTaskManager taskManager;
    private final ConfluenceConditions confluenceConditions;
    private final LicenseService licenseService;

    @Autowired
    public ConfluenceProductAccessor(MultiQueueTaskManager taskManager, ConfluenceConditions confluenceConditions,
                                     LicenseService licenseService)
    {
        this.confluenceConditions = confluenceConditions;
        this.taskManager = checkNotNull(taskManager);
        this.licenseService = licenseService;
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/marketplace_confluence";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 100;
    }

    @Override
    public String getKey()
    {
        return "confluence";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "system.browse";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.profile";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "page_id", "$!page.id",
                "page_type", "$!page.type");
    }

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        return confluenceConditions.getConditions();
    }

    @Override
    public boolean needsAdminPageNameEscaping()
    {
        return true;
    }

    @Override
    public Option<ProductLicense> getProductLicense()
    {
        AtlassianLicense atlassianLicense = licenseService.retrieveAtlassianLicense();
        return Option.option(atlassianLicense.getProductLicense(Product.CONFLUENCE));
    }
}
