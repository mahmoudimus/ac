package com.atlassian.plugin.connect.jira.module;

import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.BaseModuleList;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.util.ProductFilter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


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
        this.jiraEntityProperties = newArrayList();

    }

    public JiraConnectModuleList(JiraConnectModuleListBuilder builder)
    {
        super(builder);

        if (null == jiraEntityProperties)
        {
            this.jiraEntityProperties = newArrayList();
        }

    }


    public static JiraConnectModuleListBuilder newJiraModuleListBean()
    {
        return new JiraConnectModuleListBuilder();
    }

    public static JiraConnectModuleListBuilder newJiraModuleListBean(JiraConnectModuleList defaultBean)
    {
        return new JiraConnectModuleListBuilder(defaultBean);
    }


    public List<EntityPropertyModuleBean> getJiraEntityProperties()
    {
        return jiraEntityProperties;
    }

}
