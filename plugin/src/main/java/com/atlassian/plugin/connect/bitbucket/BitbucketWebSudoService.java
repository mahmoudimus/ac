package com.atlassian.plugin.connect.bitbucket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.spi.product.WebSudoService;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

@BitbucketComponent
public class BitbucketWebSudoService implements WebSudoService
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
