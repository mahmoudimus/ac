package com.atlassian.plugin.connect.test.pageobjects.jira;

import org.openqa.selenium.By;

/**
 * Page with legacy JIRA project reports. This page will be replaced in JIRA v6.4 by {@link com.atlassian.plugin.connect.test.pageobjects.jira.ProjectCentricNavigationProjectReportPage}.
 */
public class LegacyProjectReportPage extends ProjectReportPage
{

    public LegacyProjectReportPage(final String projectKey)
    {
        super(projectKey, By.className("version-block-container"), By.id("project-tab"), LegacyReportLink.class);
    }

    @Override
    public String getUrl()
    {
        return String.format("/browse/%s/?selectedTab=com.atlassian.jira.jira-projects-plugin:reports-panel", projectKey);
    }
}

