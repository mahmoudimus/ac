package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.labs.remoteapps.plugin.product.WebSudoElevator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
