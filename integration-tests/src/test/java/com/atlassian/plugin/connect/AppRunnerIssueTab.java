package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

import org.junit.Ignore;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean.newTabPanelBean;

@Ignore
public class AppRunnerIssueTab
{
    public static final String JIRA = "http://localhost:2990/jira";

    public static void main(String[] args)
    {
        try
        {
            ConnectCapabilitiesRunner remotePlugin = new ConnectCapabilitiesRunner(JIRA, "my-plugin")
                    .addCapability(ConnectTabPanelModuleProvider.VERSION_TAB_PANELS, newTabPanelBean()
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
