package com.atlassian.labs.remoteapps.plugin.module.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessor;

import java.net.URI;

/**
 * Macro where the content is loaded from a remote app
 */
public interface RemoteMacro extends Macro
{
    URI getBaseUrl();

    RemoteMacroInfo getRemoteMacroInfo();

    RemoteAppAccessor getRemoteAppAccessor(String pluginKey);
}
