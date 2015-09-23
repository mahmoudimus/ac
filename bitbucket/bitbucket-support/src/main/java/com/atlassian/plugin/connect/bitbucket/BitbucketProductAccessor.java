package com.atlassian.plugin.connect.bitbucket;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

import com.atlassian.bitbucket.license.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@BitbucketComponent
public class BitbucketProductAccessor implements ProductAccessor
{
    private final LicenseService licenseService;

    @Autowired
    public BitbucketProductAccessor(LicenseService licenseService)
    {
        this.licenseService = licenseService;
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
    public Optional<ProductLicense> getProductLicense()
    {
        return Optional.<ProductLicense>ofNullable(licenseService.get());
    }

    @Override
    public boolean needsAdminPageNameEscaping()
    {
        return false;
    }
}
