package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectVersionTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectVersionTabPanelModuleProvider;

/**
 * Capabilities bean for Jira Version Tab Pages. The capability JSON looks like
 * <p>
 * "versionTabPanels": [{
 * "name": {
 *     "value": "My Version Tab",
 *     "i18n": "my.tab"
 * },
 * "url": "/my-general-page",
 * "weight": 100
}]
 * </p>
 */
@CapabilitySet(key = "versionTabPanels", moduleProvider = ConnectVersionTabPanelModuleProvider.class)
public class ConnectVersionTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectVersionTabPanelCapabilityBean() {}

    public ConnectVersionTabPanelCapabilityBean(ConnectVersionTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);

    }

    public static ConnectVersionTabPanelCapabilityBeanBuilder newVersionTabPageBean()
    {
        return new ConnectVersionTabPanelCapabilityBeanBuilder();
    }

    public static ConnectVersionTabPanelCapabilityBeanBuilder newVersionTabPageBean(ConnectVersionTabPanelCapabilityBean defaultBean)
    {
        return new ConnectVersionTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
