package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;

import java.net.URI;

/**
 * Macro where the content is loaded from a remote app
 */
public interface RemoteMacro extends Macro
{
    URI getBaseUrl();

    RemoteMacroInfo getRemoteMacroInfo();
}
