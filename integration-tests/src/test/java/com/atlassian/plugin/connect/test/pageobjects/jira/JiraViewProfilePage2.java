package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteTabPanel;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Optional;

/**
 * An user ViewProfile page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewProfilePage2 extends ViewProfilePage
{
    final private Option<String> userName;

    public JiraViewProfilePage2(Option<String> userName)
    {
        this.userName = userName;
    }

    @Override
    public String getUrl()
    {
        return userName.isEmpty() ? super.getUrl() : super.getUrl() + "?name=" + userName.get();
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownMenuId)
    {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownMenuId);
    }

    public RemoteTabPanel findTabPanel(String webItemId, Optional<String> dropDownMenuId, String pageKey)
    {
        return pageBinder.bind(RemoteTabPanel.class, webItemId, dropDownMenuId, pageKey);
    }

}
