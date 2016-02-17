package com.atlassian.plugin.connect.jira.field.type;

public enum CustomFieldTypeDefinition
{
    TEXT(com.atlassian.jira.issue.customfields.impl.GenericTextCFType.class,
            "templates/field/text/view-basictext.vm",
            "templates/field/text/edit-maxlengthtext.vm",
            "templates/field/text/xml-basictext.vm");

    private final Class baseCFTypeClass;
    private final String viewTemplate;
    private final String editTemplate;
    private final String xmlTemplate;

    CustomFieldTypeDefinition(final Class baseCFTypeClass, final String viewTemplate, final String editTemplate, final String xmlTemplate)
    {
        this.baseCFTypeClass = baseCFTypeClass;
        this.viewTemplate = viewTemplate;
        this.editTemplate = editTemplate;
        this.xmlTemplate = xmlTemplate;
    }

    public String getBaseCFTypeClassFullyQualifiedName()
    {
        return baseCFTypeClass.getCanonicalName();
    }

    public String getViewTemplate()
    {
        return viewTemplate;
    }

    public String getEditTemplate()
    {
        return editTemplate;
    }

    public String getXmlTemplate()
    {
        return xmlTemplate;
    }
}
