package com.atlassian.plugin.connect.test.jira.pageobjects;

import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.pageobjects.page.AdminHomePage;

/**
 *
 */
public class JiraAdminSummaryPage implements AdminHomePage<JiraHeader> {
    @Override
    public JiraHeader getHeader() {
        return null;
    }

    @Override
    public String getUrl() {
        return "/secure/AdminSummary.jspa";
    }
}
