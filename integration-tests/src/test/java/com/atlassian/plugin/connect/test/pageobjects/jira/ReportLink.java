package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.plugin.connect.modules.beans.ReportCategory;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;

/**
 * The link to the report on {@link com.atlassian.plugin.connect.test.pageobjects.jira.ProjectReportPage}.
 */
public interface ReportLink
{
    @Init
    public void init();

    public ConnectAddOnEmbeddedTestPage open(final String pluginKey, final String moduleKey);

    public String getTitle();

    public String getDescription();

    public ReportCategory getReportCategory();

    public String getThumbnailCssClass();
}
