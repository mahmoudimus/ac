package com.atlassian.plugin.connect.spi.product;

import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.web.Condition;

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

    Map<String, Class<? extends Condition>> getConditions();

    boolean needsAdminPageNameEscaping();
}
