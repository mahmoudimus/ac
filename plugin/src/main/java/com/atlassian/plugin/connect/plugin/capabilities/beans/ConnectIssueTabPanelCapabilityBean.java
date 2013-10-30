package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectIssueTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectIssueTabPanelModuleProvider;

/**
 * Adds a tab in JIRA to the View Issue page. The new tab supplements existing tabs such as Comments and History
 * in the Activity section of the page.
 *
 */
// TODO: Note that we only need to subtype here because the annotation lives on this class rather than the descriptorFactory.
// see https://extranet.atlassian.com/display/~aholmgren/Thoughts+on+Capabilities
@CapabilitySet(key = "issueTabPanels", moduleProvider = ConnectIssueTabPanelModuleProvider.class)
public class ConnectIssueTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectIssueTabPanelCapabilityBean()
    {
    }

    public ConnectIssueTabPanelCapabilityBean(ConnectIssueTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
    }

    public static ConnectIssueTabPanelCapabilityBeanBuilder newIssueTabPanelBean()
    {
        return new ConnectIssueTabPanelCapabilityBeanBuilder();
    }

    public static ConnectIssueTabPanelCapabilityBeanBuilder newIssueTabPanelBean(ConnectIssueTabPanelCapabilityBean defaultBean)
    {
        return new ConnectIssueTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
