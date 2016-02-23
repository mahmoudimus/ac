package com.atlassian.plugin.connect.confluence;

import com.atlassian.confluence.license.LicenseService;
import com.atlassian.extras.api.AtlassianLicense;
import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 *
 */
@ConfluenceComponent
public final class ConfluenceProductAccessor implements ProductAccessor {

    private final LicenseService licenseService;

    @Autowired
    public ConfluenceProductAccessor(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public String getPreferredAdminSectionKey() {
        return "system.admin/marketplace_confluence";
    }

    @Override
    public int getPreferredAdminWeight() {
        return 100;
    }

    @Override
    public String getKey() {
        return "confluence";
    }

    @Override
    public int getPreferredGeneralWeight() {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey() {
        return "system.browse";
    }

    @Override
    public int getPreferredProfileWeight() {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey() {
        return "system.profile";
    }

    @Override
    public boolean needsAdminPageNameEscaping() {
        return true;
    }

    @Override
    public Optional<ProductLicense> getProductLicense() {
        AtlassianLicense atlassianLicense = licenseService.retrieveAtlassianLicense();
        return Optional.ofNullable(atlassianLicense.getProductLicense(Product.CONFLUENCE));
    }
}
