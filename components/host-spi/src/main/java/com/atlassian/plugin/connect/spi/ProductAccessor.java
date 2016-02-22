package com.atlassian.plugin.connect.spi;

import java.util.Optional;

import com.atlassian.extras.api.ProductLicense;

/**
 * Product-specific accessors
 */
public interface ProductAccessor extends HostProperties {
    String getPreferredAdminSectionKey();

    int getPreferredAdminWeight();

    int getPreferredGeneralWeight();

    String getPreferredGeneralSectionKey();

    int getPreferredProfileWeight();

    String getPreferredProfileSectionKey();

    boolean needsAdminPageNameEscaping();

    Optional<ProductLicense> getProductLicense();
}
