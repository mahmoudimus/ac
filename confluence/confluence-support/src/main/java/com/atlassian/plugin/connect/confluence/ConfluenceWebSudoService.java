package com.atlassian.plugin.connect.confluence;

import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.plugin.connect.spi.product.WebSudoService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ConfluenceComponent
public class ConfluenceWebSudoService implements WebSudoService
{
    private final WebSudoManager webSudoManager;

    @Autowired
    public ConfluenceWebSudoService(WebSudoManager webSudoManager)
    {
        this.webSudoManager = webSudoManager;
    }

    @Override
    public void startWebSudoSession(HttpServletRequest request, HttpServletResponse response)
    {
        webSudoManager.startSession(request, response);
    }
}
