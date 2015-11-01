package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.utils.IframeUtils;

public class ConnectGeneralTestPage extends ConnectAddOnEmbeddedTestPage implements Page
{

    public ConnectGeneralTestPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addOnKey, pageElementKey);
    }
}
