package com.atlassian.plugin.connect.modules.beans;

/**
 * This enum lists all archetypes that a remote custom field type can configure.
 * It consists of a custom field type definition and a custom field searcher.
 */
public enum IssueFieldType
{
    STRING(IssueFieldBaseTypeDefinition.TEXT, IssueFieldSearcherDefinition.EXACT_TEXT),
    TEXT(IssueFieldBaseTypeDefinition.TEXT, IssueFieldSearcherDefinition.LIKE_TEXT);

    private final IssueFieldBaseTypeDefinition type;
    private final IssueFieldSearcherDefinition searcherBase;

    private IssueFieldType(final IssueFieldBaseTypeDefinition type, final IssueFieldSearcherDefinition searcherBase)
    {
        this.type = type;
        this.searcherBase = searcherBase;
    }

    public IssueFieldBaseTypeDefinition getType()
    {
        return type;
    }

    public IssueFieldSearcherDefinition getSearcherBase()
    {
        return searcherBase;
    }


    public enum IssueFieldBaseTypeDefinition
    {
        TEXT("com.atlassian.jira.issue.customfields.impl.GenericTextCFType",
                "templates/customfield/text/view-basictext.vm",
                "templates/customfield/text/edit-maxlengthtext.vm",
                "templates/customfield/text/xml-basictext.vm");

        private final String baseCFTypeClassFullyQualifiedName;
        private final String viewTemplate;
        private final String editTemplate;
        private final String xmlTemplate;

        IssueFieldBaseTypeDefinition(final String classFullyQualifiedName, final String viewTemplate, final String editTemplate, final String xmlTemplate)
        {
            this.baseCFTypeClassFullyQualifiedName = classFullyQualifiedName;
            this.viewTemplate = viewTemplate;
            this.editTemplate = editTemplate;
            this.xmlTemplate = xmlTemplate;
        }

        public String getBaseCFTypeClassFullyQualifiedName()
        {
            return baseCFTypeClassFullyQualifiedName;
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


    public enum IssueFieldSearcherDefinition
    {
        EXACT_TEXT("com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher",
                "templates/customfield/text/view-searcher-basictext.vm",
                "templates/customfield/text/search-basictext.vm"),
        LIKE_TEXT("com.atlassian.jira.issue.customfields.searchers.TextSearcher",
                "templates/customfield/text/view-searcher-basictext.vm",
                "templates/customfield/text/search-basictext.vm");

        private final String searcherClassFullyQualifiedName;
        private final String viewTemplate;
        private final String searchTemplate;

        IssueFieldSearcherDefinition(final String searcherClassFullyQualifiedName, final String viewTemplate, final String searchTemplate)
        {
            this.searcherClassFullyQualifiedName = searcherClassFullyQualifiedName;
            this.viewTemplate = viewTemplate;
            this.searchTemplate = searchTemplate;
        }

        public String getSearcherClassFullyQualifiedName()
        {
            return searcherClassFullyQualifiedName;
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
