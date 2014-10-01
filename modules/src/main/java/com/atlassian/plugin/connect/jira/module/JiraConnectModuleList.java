package com.atlassian.plugin.connect.jira.module;

import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.BaseModuleList;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.util.ProductFilter;

import java.util.ArrayList;
import java.util.List;

//import static com.google.common.collect.Lists.newArrayList;
//import static com.google.common.collect.Maps.newHashMap;


public class JiraConnectModuleList extends BaseModuleList
{
    /**
     * The Entity Property are add-on key/value stories in certain JIRA objects, such as issues and projects.
     * @schemaTitle Entity Property
     */
    @ConnectModule(value = "com.atlassian.plugin.connect.plugin.capabilities.provider.EntityPropertyModuleProvider",
            products = {ProductFilter.JIRA})
    private List<EntityPropertyModuleBean> jiraEntityProperties;

    public JiraConnectModuleList()
    {
        this.jiraEntityProperties = new ArrayList<EntityPropertyModuleBean>();

    }

    public JiraConnectModuleList(JiraConnectModuleListBuilder builder)
    {
        super(builder);

        if (null == jiraEntityProperties)
        {
            this.jiraEntityProperties = new ArrayList<EntityPropertyModuleBean>();
        }

    }
}
