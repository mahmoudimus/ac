package com.atlassian.plugin.connect.api.web.iframe;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @since 1.0
 */
@ThreadSafe
public interface IFrameUriBuilderFactory
{
    IFrameUriBuilder builder();
}
