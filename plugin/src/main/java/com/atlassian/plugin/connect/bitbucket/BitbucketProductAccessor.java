package com.atlassian.plugin.connect.bitbucket;

import java.util.Map;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.condition.ConditionsProvider;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

import com.atlassian.bitbucket.license.LicenseService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

@BitbucketComponent
public class BitbucketProductAccessor implements ProductAccessor
{
    private final ConditionsProvider conditionsProvider;
    private final LicenseService licenseService;

    @Autowired
    public BitbucketProductAccessor(ConditionsProvider conditionsProvider, LicenseService licenseService)
    {
        this.conditionsProvider = conditionsProvider;
        this.licenseService = licenseService;
    }

    @Override
    public ConditionClassResolver getConditions()
    {
        return conditionsProvider.getConditions();
    }

    @Override
    public String getKey()
    {
        return "bitbucket";
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "atl.admin/admin-plugins-section";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 1000;
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "header.global.primary";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "bitbucket.user.profile.secondary.tabs";
    }

    @Override
    public Option<ProductLicense> getProductLicense()
    {
        return Option.<ProductLicense>option(licenseService.get());
    }

    @Override
    public boolean needsAdminPageNameEscaping()
    {
        return false;
    }
}
