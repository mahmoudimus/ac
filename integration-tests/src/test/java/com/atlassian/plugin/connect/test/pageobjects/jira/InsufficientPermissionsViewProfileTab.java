package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.ViewProfileTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;

/**
 *
 */
public class InsufficientPermissionsViewProfileTab implements ViewProfileTab
{
    private String addonKey;
    private String moduleKey;

    public InsufficientPermissionsViewProfileTab(String addonKey, String moduleKey)
    {
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
    }

    @ElementBy(id = "errorMessage")
    private PageElement errorMessage;

    @Override
    public String getUrlPart()
    {
        return ConnectPluginInfo.getPluginKey() + ":" + ModuleKeyUtils.addonAndModuleKey(addonKey,moduleKey);
    }

    @Override
    public String linkId()
    {
        return "up_" + ModuleKeyUtils.addonAndModuleKey(addonKey,moduleKey) + "_a";
    }

    @Override
    public TimedCondition isOpen()
    {
        return errorMessage.timed().isPresent();
    }

    public String getErrorMessage()
    {
        return errorMessage.getText();
    }

}
