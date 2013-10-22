package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectComponentTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectComponentTabPanelModuleProvider;

/**
 * Capabilities bean for Jira Component Tab Pages. The capability JSON looks like
 * <pre>
 * "componentTabPanels": [{
 * "name": {
 *     "value": "My Component Tab",
 *     "i18n": "my.tab"
 * },
 * "url": "/my-general-page",
 * "weight": 100
}]
 * </pre>
 */
@CapabilitySet(key = "componentTabPanels", moduleProvider = ConnectComponentTabPanelModuleProvider.class)
public class ConnectComponentTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectComponentTabPanelCapabilityBean() {}

    public ConnectComponentTabPanelCapabilityBean(ConnectComponentTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);

    }

    public static ConnectComponentTabPanelCapabilityBeanBuilder newComponentTabPanelBean()
    {
        return new ConnectComponentTabPanelCapabilityBeanBuilder();
    }

    public static ConnectComponentTabPanelCapabilityBeanBuilder newComponentTabPanelBean(ConnectComponentTabPanelCapabilityBean defaultBean)
    {
        return new ConnectComponentTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
