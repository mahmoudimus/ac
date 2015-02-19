package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraFeatureManager implements FeatureManager
{
    private final com.atlassian.jira.config.FeatureManager featureManager;

    @Autowired
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
