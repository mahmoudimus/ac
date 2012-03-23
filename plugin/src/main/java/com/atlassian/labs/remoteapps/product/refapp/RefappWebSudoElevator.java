package com.atlassian.labs.remoteapps.product.refapp;

import com.atlassian.labs.remoteapps.product.WebSudoElevator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
