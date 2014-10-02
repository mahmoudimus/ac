package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

public interface ConnectAddonBeanSerialiser
{
    ConnectAddonBean deserialise(String serialisedAddon);

    String serialise(ConnectAddonBean addon);
}





