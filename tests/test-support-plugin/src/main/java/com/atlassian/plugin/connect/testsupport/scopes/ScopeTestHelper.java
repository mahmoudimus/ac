package com.atlassian.plugin.connect.testsupport.scopes;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.io.IOException;
import java.util.Map;

public interface ScopeTestHelper
{
    Map<ScopeName, Plugin> installScopedAddons() throws IOException;

    void uninstallScopedAddons(Map<ScopeName, Plugin> installedPlugins);
}
