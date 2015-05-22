package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;

public class ConnectGeneralTestPage extends ConnectAddOnEmbeddedTestPage implements Page
{

    public ConnectGeneralTestPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/ac/"+ addOnKey + "/" + pageElementKey;
    }
}
