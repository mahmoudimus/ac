package com.atlassian.plugin.connect.api.web.iframe;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Uri Factory that creates URIs to the connects servlets or to the connect add-on.
 * @since 1.0
 */
@ThreadSafe
public interface ConnectUriFactory
{
    ConnectAddonUriBuilder createConnectAddonUriBuilder();

    String createConnectIFrameServletUri(String addOnKey, String moduleKey, ModuleContextParameters moduleContext);

    String createRedirectServletUri(String addOnKey, String moduleKey, ModuleContextParameters moduleContext);
}
