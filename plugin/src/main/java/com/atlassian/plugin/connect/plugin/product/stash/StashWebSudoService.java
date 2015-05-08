package com.atlassian.plugin.connect.plugin.product.stash;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.plugin.product.WebSudoService;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;

@StashComponent
public class StashWebSudoService implements WebSudoService
{
    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        //
    }

    @Override
    public String getWebSudoSessionKey()
    {
        return null;
    }
}
