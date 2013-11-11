package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;

public interface ConnectApplinkManager
{

    void createAppLink(Plugin plugin, String baseUrl, AuthenticationType authType, String sharedKey);
}
