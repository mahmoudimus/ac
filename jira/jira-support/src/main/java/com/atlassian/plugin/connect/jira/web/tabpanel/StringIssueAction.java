package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.issuetabpanel.IssueAction;

import java.util.Date;

/**
 *
 */
public class StringIssueAction implements IssueAction
{
    private final String html;

    public StringIssueAction(final String html)
    {
        this.html = html;
    }

    @Override
    public String getHtml()
    {
        return html;
    }

    @Override
    public Date getTimePerformed()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDisplayActionAllTab()
    {
        return false;
    }
}
