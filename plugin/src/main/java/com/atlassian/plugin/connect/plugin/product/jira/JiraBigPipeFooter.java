package com.atlassian.plugin.connect.plugin.product.jira;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.plugin.navigation.FooterModuleDescriptor;
import com.atlassian.jira.plugin.navigation.PluggableFooter;
import com.atlassian.plugin.connect.api.service.http.bigpipe.BigPipeManager;
import com.atlassian.plugin.connect.plugin.product.BigPipeFooter;

/**
 * Adds big pipe content to the footer of JIRA
 */
public class JiraBigPipeFooter implements PluggableFooter
{
    private final BigPipeFooter webPanelDelegate;

    public JiraBigPipeFooter(BigPipeManager bigPipeManager)
    {
        this.webPanelDelegate = new BigPipeFooter(bigPipeManager);
    }

    @Override
    public void init(FooterModuleDescriptor footerModuleDescriptor)
    {
    }

    @Override
    public String getFullFooterHtml(HttpServletRequest httpServletRequest)
    {
        return webPanelDelegate.getHtml(Collections.<String, Object>emptyMap());
    }

    @Override
    public String getSmallFooterHtml(HttpServletRequest httpServletRequest)
    {
        return webPanelDelegate.getHtml(Collections.<String, Object>emptyMap());
    }
}
