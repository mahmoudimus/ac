package com.atlassian.plugin.connect.plugin.iframe.render.uri;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @since 1.0
 */
@ThreadSafe
public interface IFrameUriBuilderFactory
{
    IFrameUriBuilder builder();
}
