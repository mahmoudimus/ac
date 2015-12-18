package com.atlassian.plugin.connect.api.web.iframe;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Factory of different kinds of url's used in connect.
 * @since 1.0
 */
@ThreadSafe
public interface ConnectUriFactory
{
    ConnectAddonUriBuilder createConnectAddonUriBuilder();

    String createConnectIFrameServletUri(String pluginKey, String moduleKey, ModuleContextParameters moduleContext);

    String createRedirectServletUri(String pluginKey, String moduleKey, ModuleContextParameters moduleContext);
}
