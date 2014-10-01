package com.atlassian.plugin.connect.jira.module;

import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleListBuilder;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class JiraConnectModuleListBuilder extends BaseModuleListBuilder<JiraConnectModuleListBuilder, JiraConnectModuleList>
{
    private List<EntityPropertyModuleBean> jiraEntityProperties;

    public JiraConnectModuleListBuilder()
    {
    }

    public JiraConnectModuleListBuilder(JiraConnectModuleList defaultBean)
    {
        jiraEntityProperties = defaultBean.getJiraEntityProperties();
    }

    public JiraConnectModuleListBuilder withJiraEntityProperties(EntityPropertyModuleBean... beans)
    {
        this.jiraEntityProperties = ImmutableList.copyOf(beans);
        return this;
    }

    @Override
    public JiraConnectModuleList build()
    {
        return new JiraConnectModuleList(this);
    }
}
