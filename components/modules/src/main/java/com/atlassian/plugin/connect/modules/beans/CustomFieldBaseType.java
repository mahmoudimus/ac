package com.atlassian.plugin.connect.modules.beans;

/**
 * This enum lists all archetypes that a remote custom field type can configure.
 * It consists of a custom field type definition and a custom field searcher.
 */
public enum CustomFieldBaseType
{
    STRING(CustomFieldBaseTypeDefinition.TEXT, CustomFieldSearcherDefinition.EXACT_TEXT),
    TEXT(CustomFieldBaseTypeDefinition.TEXT, CustomFieldSearcherDefinition.LIKE_TEXT);

    private final CustomFieldBaseTypeDefinition type;
    private final CustomFieldSearcherDefinition searcherBase;

    private CustomFieldBaseType(final CustomFieldBaseTypeDefinition type, final CustomFieldSearcherDefinition searcherBase)
    {
        this.type = type;
        this.searcherBase = searcherBase;
    }

    public CustomFieldBaseTypeDefinition getType()
    {
        return type;
    }

    public CustomFieldSearcherDefinition getSearcherBase()
    {
        return searcherBase;
    }


    public enum CustomFieldBaseTypeDefinition
    {
        TEXT("com.atlassian.plugin.connect.jira.customfield.type.TextConnectCustomFieldType",
                "templates/plugins/fields/view/view-basictext.vm",
                "templates/plugins/fields/edit/edit-maxlengthtext.vm");

        private final String classFQN;
        private final String viewTemplate;
        private final String editTemplate;

        CustomFieldBaseTypeDefinition(final String classFQN, final String viewTemplate, final String editTemplate)
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


    public enum CustomFieldSearcherDefinition
    {
        EXACT_TEXT("com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher",
                "templates/plugins/fields/view-searcher/view-searcher-basictext.vm",
                "templates/plugins/fields/edit-searcher/search-basictext.vm"),
        LIKE_TEXT("com.atlassian.jira.issue.customfields.searchers.TextSearcher",
                "templates/plugins/fields/view-searcher/view-searcher-basictext.vm",
                "templates/plugins/fields/edit-searcher/search-basictext.vm");

        private final String classFQN;
        private final String viewTemplate;
        private final String searchTemplate;

        CustomFieldSearcherDefinition(final String classFQN, final String viewTemplate, final String searchTemplate)
        {
            this.classFQN = classFQN;
            this.viewTemplate = viewTemplate;
            this.searchTemplate = searchTemplate;
        }

        public String getClassFQN()
        {
            return classFQN;
        }

        public String getViewTemplate()
        {
            return viewTemplate;
        }

        public String getSearchTemplate()
        {
            return searchTemplate;
        }
    }

}
