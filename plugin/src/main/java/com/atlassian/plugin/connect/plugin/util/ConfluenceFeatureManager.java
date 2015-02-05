package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.confluence.setup.settings.CoreFeaturesManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

@ConfluenceComponent
public class ConfluenceFeatureManager implements FeatureManager
{
    private final CoreFeaturesManager coreFeaturesManager;

    public ConfluenceFeatureManager(final CoreFeaturesManager coreFeaturesManager)
    {
        this.coreFeaturesManager = coreFeaturesManager;
    }

    @Override
    public boolean isOnDemand()
    {
        return coreFeaturesManager.isOnDemand();
    }
}
