package com.atlassian.plugin.connect.spi;

import com.atlassian.extras.api.ProductLicense;

import java.util.Optional;

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
