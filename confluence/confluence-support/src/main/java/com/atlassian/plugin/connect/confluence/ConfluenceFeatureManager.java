package com.atlassian.plugin.connect.confluence;

import com.atlassian.confluence.setup.settings.CoreFeaturesManager;
import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ConfluenceFeatureManager implements FeatureManager
{
    private final CoreFeaturesManager coreFeaturesManager;

    @Autowired
    public ConfluenceFeatureManager(final CoreFeaturesManager coreFeaturesManager)
    {
        this.coreFeaturesManager = coreFeaturesManager;
    }

    @Override
    public boolean isOnDemand()
    {
        return coreFeaturesManager.isOnDemand();
    }

    @Override
    public boolean isPermissionsManagedByUM()
    {
        return false;
    }
}
