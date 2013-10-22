package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectProjectTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectProjectTabPanelModuleProvider;

/**
 * Capabilities bean for Jira Project Tab Pages. The capability JSON looks like
 * <p>
 * <pre>
 * "projectTabPanels": [{
 *   "name": {
 *     "value": "My Project Tab",
 *     "i18n": "my.tab"
 *   },
 *   "url": "/my-general-page",
 *   "weight": 100
 * }]
 * </pre>
 * </p>
 */
@CapabilitySet(key = "projectTabPanels", moduleProvider = ConnectProjectTabPanelModuleProvider.class)
public class ConnectProjectTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    public ConnectProjectTabPanelCapabilityBean() {}

    public ConnectProjectTabPanelCapabilityBean(ConnectProjectTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
    }

    public static ConnectProjectTabPanelCapabilityBeanBuilder newProjectTabPanelBean()
    {
        return new ConnectProjectTabPanelCapabilityBeanBuilder();
    }

    public static ConnectProjectTabPanelCapabilityBeanBuilder newProjectTabPanelBean(ConnectProjectTabPanelCapabilityBean defaultBean)
    {
        return new ConnectProjectTabPanelCapabilityBeanBuilder(defaultBean);
    }

}
