package com.atlassian.plugin.connect.plugin.product.confluence;

import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.plugin.connect.plugin.product.WebSudoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfluenceWebSudoService implements WebSudoService
{
    private final WebSudoManager webSudoManager;

    public ConfluenceWebSudoService(WebSudoManager webSudoManager)
    {
        this.webSudoManager = webSudoManager;
    }

    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        webSudoManager.startSession(request, response);
    }

    @Override
    public String getWebSudoSessionKey()
    {
        return "confluence.websudo.timestamp"; // from Confluence's DefaultWebSudoManager
    }
}
