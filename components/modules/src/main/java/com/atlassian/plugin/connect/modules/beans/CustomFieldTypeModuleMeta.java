package com.atlassian.plugin.connect.modules.beans;

public class CustomFieldTypeModuleMeta extends ConnectModuleMeta<CustomFieldTypeModuleBean>
{
    public CustomFieldTypeModuleMeta()
    {
        super("jiraCustomFieldTypes", CustomFieldTypeModuleBean.class);
    }
}
