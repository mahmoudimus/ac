package com.atlassian.plugin.connect.plugin.product.stash;

import java.util.Map;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.extras.api.stash.StashLicense;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.web.Condition;

import com.atlassian.stash.license.LicenseService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

@StashComponent
public class StashProductAccessor implements ProductAccessor
{
    private final LicenseService licenseService;

    @Autowired
    public StashProductAccessor(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        return ImmutableMap.of();
    }

    @Override
    public String getKey()
    {
        return "stash";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of();
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
        return "stash.user.profile.secondary.tabs";
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
