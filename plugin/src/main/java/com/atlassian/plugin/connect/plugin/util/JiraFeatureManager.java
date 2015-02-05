package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

@JiraComponent
public class JiraFeatureManager implements FeatureManager
{
    private final com.atlassian.jira.config.FeatureManager featureManager;

    public JiraFeatureManager(final com.atlassian.jira.config.FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    @Override
    public boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }
}
