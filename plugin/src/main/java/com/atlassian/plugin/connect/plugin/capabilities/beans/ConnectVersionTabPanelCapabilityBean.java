package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectVersionTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectVersionTabPanelModuleProvider;

/**
 * Capabilities bean for Jira Version Tab Pages. The capability JSON looks like
 * <pre>
 * "versionTabPanels": [{
 * "name": {
 *     "value": "My Version Tab",
 *     "i18n": "my.tab"
 * },
 * "url": "/my-general-page",
 * "weight": 100
}]
 * </pre>
 */
@CapabilitySet(key = "versionTabPanels", moduleProvider = ConnectVersionTabPanelModuleProvider.class)
public class ConnectVersionTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectVersionTabPanelCapabilityBean() {}

    public ConnectVersionTabPanelCapabilityBean(ConnectVersionTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);

    }

    public static ConnectVersionTabPanelCapabilityBeanBuilder newVersionTabPanelBean()
    {
        return new ConnectVersionTabPanelCapabilityBeanBuilder();
    }

    public static ConnectVersionTabPanelCapabilityBeanBuilder newVersionTabPanelBean(ConnectVersionTabPanelCapabilityBean defaultBean)
    {
        return new ConnectVersionTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
