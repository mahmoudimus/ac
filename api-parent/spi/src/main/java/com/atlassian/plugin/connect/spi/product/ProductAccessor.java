package com.atlassian.plugin.connect.spi.product;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.host.HostProperties;

import java.util.Map;

/**
 * Product-specific accessors
 */
public interface ProductAccessor extends HostProperties
{
    String getPreferredAdminSectionKey();

    int getPreferredAdminWeight();

    int getPreferredGeneralWeight();

    String getPreferredGeneralSectionKey();

    int getPreferredProfileWeight();

    String getPreferredProfileSectionKey();

    Map<String, String> getLinkContextParams();

    ConditionClassResolver getConditions();

    boolean needsAdminPageNameEscaping();

    Option<ProductLicense> getProductLicense();
}
