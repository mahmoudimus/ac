package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.labs.remoteapps.plugin.product.WebSudoElevator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
