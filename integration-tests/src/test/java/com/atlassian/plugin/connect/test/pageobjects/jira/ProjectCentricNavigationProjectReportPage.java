package com.atlassian.plugin.connect.test.pageobjects.jira;

import org.openqa.selenium.By;

/**
 * Project centric navigation page with JIRA project reports. It requires jira-projects-plugin v1.4.
 */
public class ProjectCentricNavigationProjectReportPage extends ProjectReportPage
{

    public ProjectCentricNavigationProjectReportPage(final String projectKey)
    {
        super(projectKey, By.className("reports__list__item"), By.id("project-tab"), ProjectCentricNavigationReportLink.class);
    }

    @Override
    public String getUrl()
    {
        return String.format("/projects/%s?selectedItem=com.atlassian.jira.jira-projects-plugin:report-page", projectKey);
    }
}

