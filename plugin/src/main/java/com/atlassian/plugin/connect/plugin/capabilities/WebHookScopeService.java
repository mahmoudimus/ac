package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

public interface WebHookScopeService
{
    /**
     * What scope must an add-on possess in order to receive the named web hook?
     * @param webHookKey The {@link com.atlassian.plugin.connect.modules.beans.WebHookModuleBean#getEvent()}
     * @return The {@link ScopeName} that the add-on must request in order to be sent this web hook
     */
    ScopeName getRequiredScope(String webHookKey);
}
