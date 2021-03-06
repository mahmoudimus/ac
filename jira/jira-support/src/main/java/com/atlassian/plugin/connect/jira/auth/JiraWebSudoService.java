package com.atlassian.plugin.connect.jira.auth;

import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.plugin.connect.spi.auth.WebSudoService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@JiraComponent
public class JiraWebSudoService implements WebSudoService {
    private final InternalWebSudoManager jiraWebSudoManager;

    @Autowired
    public JiraWebSudoService(InternalWebSudoManager jiraWebSudoManager) {
        this.jiraWebSudoManager = jiraWebSudoManager;
    }

    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response) {
        jiraWebSudoManager.startSession(request, response);
    }
}
