package com.atlassian.plugin.connect.modules.beans;

/**
 * This enum lists all types that a remote issue field can have.
 * It consists of an issue field type definition and a issue field searcher.
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
                "templates/field/text/view-basictext.vm",
                "templates/field/text/edit-maxlengthtext.vm",
                "templates/field/text/xml-basictext.vm");

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
                "templates/field/searcher/view-searcher-basictext.vm",
                "templates/field/searcher/search-basictext.vm"),
        LIKE_TEXT("com.atlassian.jira.issue.customfields.searchers.TextSearcher",
                "templates/field/searcher/view-searcher-basictext.vm",
                "templates/field/searcher/search-basictext.vm");

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
