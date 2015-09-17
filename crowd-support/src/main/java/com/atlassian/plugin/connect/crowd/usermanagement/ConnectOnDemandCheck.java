package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.plugin.connect.spi.product.FeatureManager;

import org.springframework.beans.factory.annotation.Autowired;

public class ConnectOnDemandCheck
{
    private final FeatureManager featureManager;

    @Autowired
    public ConnectOnDemandCheck(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    public boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }
}
