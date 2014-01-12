package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import org.junit.Ignore;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelModuleBean.newTabPanelBean;

@Ignore
public class AppRunnerIssueTab
{
    public static final String JIRA = "http://localhost:2990/jira";

    public static void main(String[] args)
    {
        try
        {
            ConnectRunner remotePlugin = new ConnectRunner(JIRA, "my-plugin")
                    .addModule(ConnectTabPanelModuleProvider.VERSION_TAB_PANELS, newTabPanelBean()
                            .withName(new I18nProperty("My Version Tab", "My Version Tab"))
                            .withWeight(1)
                            .withUrl("/irwi")
                            .build())
                    .start();
            while (true)
            {
                //do nothing
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
