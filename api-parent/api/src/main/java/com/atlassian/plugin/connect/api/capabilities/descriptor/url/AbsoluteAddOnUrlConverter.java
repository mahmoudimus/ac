package com.atlassian.plugin.connect.api.capabilities.descriptor.url;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import java.net.URISyntaxException;

/**
 * Converts URLs specified by Add-On descriptors into absolute URLs.
 * Either by resolving them against the Add-On base url, or by just returning them if they
 * were already absolute.
 */
public interface AbsoluteAddOnUrlConverter
{
    String getAbsoluteUrl(String pluginKey, String url) throws URISyntaxException;
    String getAbsoluteUrl(ConnectAddonBean addon, String url);
}
