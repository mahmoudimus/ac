package com.atlassian.plugin.connect.spi;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.web.condition.ConditionClassResolver;

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

    ConditionClassResolver getConditions();

    boolean needsAdminPageNameEscaping();

    Option<ProductLicense> getProductLicense();
}
