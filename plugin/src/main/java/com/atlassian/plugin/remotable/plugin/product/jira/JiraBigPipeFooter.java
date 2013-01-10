package com.atlassian.plugin.remotable.plugin.product.jira;

import com.atlassian.jira.plugin.navigation.FooterModuleDescriptor;
import com.atlassian.jira.plugin.navigation.PluggableFooter;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipe;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Footer that adds some javascript to handle big pipe requests that have completed.
 */
public class JiraBigPipeFooter implements PluggableFooter
{
    private final BigPipe bigPipe;
    private final WebResourceUrlProvider webResourceUrlProvider;

    private static final Logger log = LoggerFactory.getLogger(JiraBigPipeFooter.class);

    public JiraBigPipeFooter(BigPipe bigPipe, WebResourceUrlProvider webResourceUrlProvider)
    {
        this.bigPipe = checkNotNull(bigPipe);
        this.webResourceUrlProvider = checkNotNull(webResourceUrlProvider);
    }

    @Override
    public void init(FooterModuleDescriptor descriptor)
    {
    }

    @Override
    public String getFullFooterHtml(HttpServletRequest request)
    {
        return getFooterHtml();
    }

    @Override
    public String getSmallFooterHtml(HttpServletRequest request)
    {
        return getFooterHtml();
    }

    private String getFooterHtml()
    {
        String bigPipeJs = webResourceUrlProvider.getStaticPluginResourceUrl(
                "com.atlassian.labs.remoteapps-plugin:big-pipe", "big-pipe.js", UrlMode.AUTO
        );
        String json = bigPipe.consumeContent();
        return "<script>" +
                "(function(global) {" +
                "var AP = global.AP = global.AP || {};" +
                "if (AP.RemoteConditions) AP.RemoteConditions.hide();" +
                "var contents = " + json + ";" +
                "if (AP.BigPipe) AP.BigPipe.processContents(contents);" +
                "else AJS.$.getScript('" + bigPipeJs + "')" +
                ".done(function(){AP.BigPipe.processContents(contents);});" +
                "})(this);" +
                "</script>";
    }

}
