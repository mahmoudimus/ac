package com.atlassian.plugin.connect.jira;

import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.connect.spi.product.WebSudoService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraWebSudoService implements WebSudoService
{
    private final InternalWebSudoManager jiraWebSudoManager;

    @Autowired
    public JiraWebSudoService(InternalWebSudoManager jiraWebSudoManager)
    {
        this.jiraWebSudoManager = jiraWebSudoManager;
    }
    
    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        jiraWebSudoManager.startSession(request, response);
    }

    @Override
    public String getWebSudoSessionKey()
    {
        return SessionKeys.WEBSUDO_TIMESTAMP;
    }
}
