package com.atlassian.plugin.connect.plugin.module.confluence;

import java.net.URI;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;

/**
 * Macro where the content is loaded from a remote plugin
 */
public interface RemoteMacro extends Macro
{
    URI getBaseUrl();

    RemoteMacroInfo getRemoteMacroInfo();

    RemotablePluginAccessor getRemotablePluginAccessor(String pluginKey);
}
