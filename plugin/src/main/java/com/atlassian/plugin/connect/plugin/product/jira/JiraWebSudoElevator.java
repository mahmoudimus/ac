package com.atlassian.plugin.connect.plugin.product.jira;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.plugin.connect.plugin.product.WebSudoElevator;

public class JiraWebSudoElevator implements WebSudoElevator
{
    private final InternalWebSudoManager jiraWebSudoManager;

    public JiraWebSudoElevator(InternalWebSudoManager jiraWebSudoManager)
    {
        this.jiraWebSudoManager = jiraWebSudoManager;
    }
    
    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        jiraWebSudoManager.startSession(request, response);
    }
}
