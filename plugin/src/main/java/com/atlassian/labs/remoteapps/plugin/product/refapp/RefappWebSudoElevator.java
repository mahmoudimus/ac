package com.atlassian.labs.remoteapps.plugin.product.refapp;

import com.atlassian.labs.remoteapps.plugin.product.WebSudoElevator;
import com.atlassian.refapp.auth.external.WebSudoSessionManager;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RefappWebSudoElevator implements WebSudoElevator
{
    private final WebSudoSessionManager refAppWebSudoSessionManager;

    public RefappWebSudoElevator(WebSudoSessionManager refAppWebSudoSessionManager)
    {
        this.refAppWebSudoSessionManager = refAppWebSudoSessionManager;
    }

    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        refAppWebSudoSessionManager.createWebSudoSession(request);
    }
}
