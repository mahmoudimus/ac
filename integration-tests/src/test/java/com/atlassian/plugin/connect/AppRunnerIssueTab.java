package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.server.ConnectCapabilitiesRunner;

public class AppRunnerIssueTab
{
    public static final String JIRA = "http://localhost:2990/jira";

    public static void main(String[] args)
    {
        try
        {
            ConnectCapabilitiesRunner remotePlugin = new ConnectCapabilitiesRunner(JIRA,"my-plugin")
                    .addCapability(ConnectIssueTabPanelCapabilityBean.newIssueTabPageBean()
                            .withName(new I18nProperty("My Tab","My Tab"))
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
