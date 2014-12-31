package com.atlassian.plugin.connect.plugin.product.jira;

import com.atlassian.extras.api.Product;
import com.atlassian.extras.api.ProductLicense;
import com.atlassian.extras.api.jira.JiraLicense;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.plugin.connect.modules.beans.JiraConditions;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@JiraComponent
public final class JiraProductAccessor implements ProductAccessor
{
    private final JiraConditions jiraConditions;
    private final JiraLicenseService licenseService;

    @Autowired
    public JiraProductAccessor(JiraConditions jiraConditions, JiraLicenseService licenseService)
    {
        this.jiraConditions = jiraConditions;
        this.licenseService = licenseService;
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "advanced_menu_section/advanced_section";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 150;
    }

    @Override
    public String getKey()
    {
        return "jira";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "system.top.navigation.bar";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.user.options/personal";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "project_id", "$!helper.project.id",
                "project_key", "$!helper.project.key",
                "issue_id", "$!issue.id",
                "issue_key", "$!issue.key");
    }

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        return jiraConditions.getConditions();
    }

    @Override
    public boolean needsAdminPageNameEscaping()
    {
        return false;
    }

    @Override
    public Option<ProductLicense> getProductLicense()
    {
        Iterable<LicenseDetails> licenses = licenseService.getLicenses();
        Option<ProductLicense> jiraProductLicenseOption = Option.none();
        for (LicenseDetails licenseDetails : licenses)
        {
            ProductLicense productLicense = licenseDetails.getJiraLicense();
            if (productLicense.getProduct().equals(Product.JIRA))
            {
                jiraProductLicenseOption = Option.some(productLicense);
            }
        }
        return jiraProductLicenseOption;
    }
}
