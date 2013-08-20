package com.atlassian.plugin.connect.plugin.product.confluence;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.plugin.connect.plugin.product.WebSudoElevator;

public class ConfluenceWebSudoElevator implements WebSudoElevator
{
    private final WebSudoManager webSudoManager;

    public ConfluenceWebSudoElevator(WebSudoManager webSudoManager)
    {
        this.webSudoManager = webSudoManager;
    }

    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        webSudoManager.startSession(request, response);
    }
}
