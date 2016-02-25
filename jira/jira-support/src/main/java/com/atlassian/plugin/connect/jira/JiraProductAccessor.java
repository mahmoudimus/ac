package com.atlassian.plugin.connect.jira;

import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@JiraComponent
public final class JiraProductAccessor implements ProductAccessor {

    private final JiraLicenseService licenseService;

    @Autowired
    public JiraProductAccessor(JiraLicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public String getPreferredAdminSectionKey() {
        return "advanced_menu_section/advanced_section";
    }

    @Override
    public int getPreferredAdminWeight() {
        return 150;
    }

    @Override
    public String getKey() {
        return "jira";
    }

    @Override
    public int getPreferredGeneralWeight() {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey() {
        return "system.top.navigation.bar";
    }

    @Override
    public int getPreferredProfileWeight() {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey() {
        return "system.user.options/personal";
    }

    @Override
    public boolean needsAdminPageNameEscaping() {
        return false;
    }

    @Override
    public Optional<ProductLicense> getProductLicense() {
        Iterable<LicenseDetails> licenses = licenseService.getLicenses();
        Optional<ProductLicense> jiraProductLicenseOption = Optional.empty();
        for (LicenseDetails licenseDetails : licenses) {
            ProductLicense productLicense = licenseDetails.getJiraLicense();
            if (productLicense.getProduct().getName().equals(Product.JIRA.getName())) {
                jiraProductLicenseOption = Optional.of(productLicense);
            }
        }
        return jiraProductLicenseOption;
    }
}
