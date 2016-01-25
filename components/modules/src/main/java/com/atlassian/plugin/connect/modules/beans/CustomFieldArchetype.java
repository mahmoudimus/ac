package com.atlassian.plugin.connect.modules.beans;

/**
 * This enum lists all archetypes that a remote custom field type can configure.
 * It consists of a custom field type definition and a custom field searcher.
 */
public enum CustomFieldArchetype
{
    STRING(CustomFieldBaseType.TEXT, CustomFieldSearcherBase.EXACT_TEXT),
    TEXT(CustomFieldBaseType.TEXT, CustomFieldSearcherBase.LIKE_TEXT);

    private final CustomFieldBaseType type;
    private final CustomFieldSearcherBase searcherBase;

    CustomFieldArchetype(final CustomFieldBaseType type, final CustomFieldSearcherBase searcherBase)
    {
        this.type = type;
        this.searcherBase = searcherBase;
    }

    public CustomFieldBaseType getType()
    {
        return type;
    }

    public CustomFieldSearcherBase getSearcherBase()
    {
        return searcherBase;
    }


    public enum CustomFieldBaseType
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


    public enum CustomFieldSearcherBase
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


        CustomFieldSearcherBase(final String classFQN, final String viewTemplate, final String searchTemplate)
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
