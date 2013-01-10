package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.HtmlPromise;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;

import java.security.SecureRandom;
import java.util.Map;

import static java.util.Collections.singletonMap;

class DefaultHtmlPromise extends ForwardingPromise<String> implements HtmlPromise, MetadataProvider
{
    private static final SecureRandom secureRandom = SecureRandomFactory.newInstance();

    private final Promise<String> delegate;
    private final String contentId;

    public DefaultHtmlPromise(Promise<String> delegate)
    {
        this.contentId = "bp-" + Long.toHexString(Math.abs(secureRandom.nextLong()));
        this.delegate = delegate;
    }

    public String getInitialContent()
    {
        if (delegate().isDone())
        {
            return "<span id=\"" + contentId + "\">" + delegate().claim() + "</span>";
        }
        else
        {
            return "<span id=\"" + contentId + "\" class=\"bp-loading\"></span>";
        }
    }

    @Override
    public Map<String, String> getMetadata()
    {
        return singletonMap("contentId", contentId);
    }

    @Override
    protected Promise<String> delegate()
    {
        return delegate;
    }
}
