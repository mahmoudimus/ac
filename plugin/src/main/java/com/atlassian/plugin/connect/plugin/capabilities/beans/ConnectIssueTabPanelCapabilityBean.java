package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectIssueTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectIssueTabPanelModuleProvider;

/**
 * Capabilities bean for Jira Issue Tab Pages. The capability JSON looks like
 * <p>
 * "issueTabPanels": [{
 * "name": {
 *     "value": "My Issue Tab",
 *     "i18n": "my.tab"
 * },
 * "url": "/my-general-page",
 * "weight": 100
}]
 * </p>
 */
// TODO: Note that we only need to subtype here because the annotation lives on this class rather than the descriptorFactory.
// see https://extranet.atlassian.com/display/~aholmgren/Thoughts+on+Capabilities
@CapabilitySet(key = "issueTabPanels", moduleProvider = ConnectIssueTabPanelModuleProvider.class)
public class ConnectIssueTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectIssueTabPanelCapabilityBean() {}

    public ConnectIssueTabPanelCapabilityBean(ConnectIssueTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);

    }

    public static ConnectIssueTabPanelCapabilityBeanBuilder newIssueTabPageBean()
    {
        return new ConnectIssueTabPanelCapabilityBeanBuilder();
    }

    public static ConnectIssueTabPanelCapabilityBeanBuilder newIssueTabPageBean(ConnectIssueTabPanelCapabilityBean defaultBean)
    {
        return new ConnectIssueTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
