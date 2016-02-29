package com.atlassian.plugin.connect.jira.field.type;

public enum SearcherDefinition {
    EXACT_TEXT(com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher.class,
            "templates/field/searcher/view-searcher-basictext.vm",
            "templates/field/searcher/search-basictext.vm"),
    LIKE_TEXT(com.atlassian.jira.issue.customfields.searchers.TextSearcher.class,
            "templates/field/searcher/view-searcher-basictext.vm",
            "templates/field/searcher/search-basictext.vm");

    private final Class searcherClassFullyQualifiedName;
    private final String viewTemplate;
    private final String searchTemplate;

    SearcherDefinition(final Class searcherClassFullyQualifiedName, final String viewTemplate, final String searchTemplate) {
        this.searcherClassFullyQualifiedName = searcherClassFullyQualifiedName;
        this.viewTemplate = viewTemplate;
        this.searchTemplate = searchTemplate;
    }

    public String getSearcherClassFullyQualifiedName() {
        return searcherClassFullyQualifiedName.getCanonicalName();
    }

    public String getViewTemplate() {
        return viewTemplate;
    }

    public String getSearchTemplate() {
        return searchTemplate;
    }
}
