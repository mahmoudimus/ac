package com.atlassian.plugin.connect.jira.customfield;

enum CustomFieldSearcherBase
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
