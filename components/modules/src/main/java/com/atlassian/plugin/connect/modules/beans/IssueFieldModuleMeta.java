package com.atlassian.plugin.connect.modules.beans;

public class IssueFieldModuleMeta extends ConnectModuleMeta<IssueFieldModuleBean>
{
    public IssueFieldModuleMeta()
    {
        super("jiraIssueFields", IssueFieldModuleBean.class);
    }
}
