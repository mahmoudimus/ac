package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilitySet;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectProjectAdminTabPanelCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectProjectAdminTabPanelModuleProvider;

/**
 * Capabilities bean for Jira ProjectAdmin Tab Pages. The capability JSON looks like
 * <p>
 * <pre>
 * "projectAdminTabPanels": [{
 *   "name": {
 *     "value": "My ProjectAdmin Tab",
 *     "i18n": "my.tab"
 *   },
 *   "url": "/my-general-page",
 *   "weight": 100
 *   "location" : "projectgroup4"
 * }]
 * </pre>
 * </p>
 */
@CapabilitySet(key = "projectAdminTabPanels", moduleProvider = ConnectProjectAdminTabPanelModuleProvider.class)
public class ConnectProjectAdminTabPanelCapabilityBean extends AbstractConnectTabPanelCapabilityBean
{
    private static final String PROJECT_CONFIG_TAB_LOCATION_PREFIX = "atl.jira.proj.config/";
    private String location;

    public ConnectProjectAdminTabPanelCapabilityBean() {}

    public ConnectProjectAdminTabPanelCapabilityBean(ConnectProjectAdminTabPanelCapabilityBeanBuilder builder)
    {
        super(builder);
        // TODO: is location mandatory?
        if (null == location)
        {
            this.location = "";
        }
    }

    public static ConnectProjectAdminTabPanelCapabilityBeanBuilder newProjectAdminTabPanelBean()
    {
        return new ConnectProjectAdminTabPanelCapabilityBeanBuilder();
    }

    public static ConnectProjectAdminTabPanelCapabilityBeanBuilder newProjectAdminTabPanelBean(ConnectProjectAdminTabPanelCapabilityBean defaultBean)
    {
        return new ConnectProjectAdminTabPanelCapabilityBeanBuilder(defaultBean);
    }

    public String getLocation()
    {
        return location;
    }

    public String getAbsoluteLocation()
    {
        return PROJECT_CONFIG_TAB_LOCATION_PREFIX + location;
    }

}
