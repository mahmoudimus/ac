package com.atlassian.plugin.connect.plugin.product.stash;

import java.util.Map;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableMap;

@StashComponent
public class StashProductAccessor implements ProductAccessor
{
    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        return null;
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
        return "system.admin/marketplace_stash";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 100;
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "some.sections";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "some.profile";
    }

    @Override
    public Option<ProductLicense> getProductLicense()
    {
        return Option.none();
    }

    @Override
    public boolean needsAdminPageNameEscaping()
    {
        return false;
    }
}
