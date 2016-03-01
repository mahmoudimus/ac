package com.atlassian.plugin.connect.modules.beans;

public class ProjectPermissionModuleMeta extends ConnectModuleMeta<ProjectPermissionModuleBean> {

    public ProjectPermissionModuleMeta() {
        super("jiraProjectPermissions", ProjectPermissionModuleBean.class);
    }
}