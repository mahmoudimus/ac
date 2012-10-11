package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.remotable.plugin.RemotablePluginAccessor;

import java.net.URI;

/**
 * Macro where the content is loaded from a remote plugin
 */
public interface RemoteMacro extends Macro
{
    URI getBaseUrl();

    RemoteMacroInfo getRemoteMacroInfo();

    RemotablePluginAccessor getRemotablePluginAccessor(String pluginKey);
}
