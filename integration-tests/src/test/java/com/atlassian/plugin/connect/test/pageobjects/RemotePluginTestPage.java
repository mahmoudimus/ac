package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

@XmlDescriptor(comment="migrate to ConnectAddOnEmbeddedTestPage")
public class RemotePluginTestPage extends RemotePluginEmbeddedTestPage
{
    public RemotePluginTestPage(String pageKey)
    {
        super(pageKey);
    }

    public RemotePluginTestPage(String pageKey, String extraPrefix)
    {
        super(pageKey, extraPrefix);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
