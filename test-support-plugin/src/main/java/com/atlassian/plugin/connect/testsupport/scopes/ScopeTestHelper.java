package com.atlassian.plugin.connect.testsupport.scopes;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import org.hamcrest.Matcher;

import java.io.IOException;
import java.util.Map;

public interface ScopeTestHelper
{
    Map<ScopeName, Plugin> installScopedAddOns() throws IOException;

    void uninstallScopedAddOns(Map<ScopeName, Plugin> installedPlugins);
}
