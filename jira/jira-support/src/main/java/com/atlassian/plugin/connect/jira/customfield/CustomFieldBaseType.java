package com.atlassian.plugin.connect.jira.customfield;

enum CustomFieldBaseType
{
    TEXT("com.atlassian.jira.issue.customfields.impl.GenericTextCFType",
            "templates/plugins/fields/view/view-basictext.vm",
            "templates/plugins/fields/edit/edit-maxlengthtext.vm");

    private final String classFQN;
    private final String viewTemplate;
    private final String editTemplate;


    CustomFieldBaseType(final String classFQN, final String viewTemplate, final String editTemplate)
    {
        this.classFQN = classFQN;
        this.viewTemplate = viewTemplate;
        this.editTemplate = editTemplate;
    }

    public String getClassFQN()
    {
        return classFQN;
    }

    public String getViewTemplate()
    {
        return viewTemplate;
    }

    public String getEditTemplate()
    {
        return editTemplate;
    }
}
