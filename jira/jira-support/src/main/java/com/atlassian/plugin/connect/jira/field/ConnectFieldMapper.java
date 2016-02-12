package com.atlassian.plugin.connect.jira.field;

import com.atlassian.plugin.connect.modules.beans.IssueFieldType;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.plugin.connect.jira.field.ConnectFieldMapper.ConnectFieldTypeDefinition.mapping;
import static com.atlassian.plugin.connect.modules.beans.IssueFieldType.STRING;
import static com.atlassian.plugin.connect.modules.beans.IssueFieldType.TEXT;

@JiraComponent
public class ConnectFieldMapper
{
    private final ImmutableMap<IssueFieldType, ConnectFieldTypeDefinition> map;

    public ConnectFieldMapper()
    {
        map = ImmutableMap.<IssueFieldType,ConnectFieldTypeDefinition>builder()
                .put(STRING, mapping(BaseTypeDefinition.TEXT, SearcherDefinition.EXACT_TEXT))
                .put(TEXT, mapping(BaseTypeDefinition.TEXT, SearcherDefinition.LIKE_TEXT))
                .build();
    }

    public ConnectFieldTypeDefinition getMapping(IssueFieldType fieldType)
    {
        return map.get(fieldType);
    }

    public static class ConnectFieldTypeDefinition
    {
        private final BaseTypeDefinition typeDefinition;
        private final SearcherDefinition searcherDefinition;

        public ConnectFieldTypeDefinition(final BaseTypeDefinition typeDefinition, final SearcherDefinition searcherDefinition)
        {
            this.typeDefinition = typeDefinition;
            this.searcherDefinition = searcherDefinition;
        }

        public static ConnectFieldTypeDefinition mapping(final BaseTypeDefinition typeDefinition, final SearcherDefinition searcherDefinition)
        {
            return new ConnectFieldTypeDefinition(typeDefinition, searcherDefinition);
        }

        public BaseTypeDefinition getType()
        {
            return typeDefinition;
        }

        public SearcherDefinition getSearcherBase()
        {
            return searcherDefinition;
        }
    }

    public enum BaseTypeDefinition
    {
        TEXT(com.atlassian.jira.issue.customfields.impl.GenericTextCFType.class,
                "templates/field/text/view-basictext.vm",
                "templates/field/text/edit-maxlengthtext.vm",
                "templates/field/text/xml-basictext.vm");

        private final Class baseCFTypeClass;
        private final String viewTemplate;
        private final String editTemplate;
        private final String xmlTemplate;

        BaseTypeDefinition(final Class baseCFTypeClass, final String viewTemplate, final String editTemplate, final String xmlTemplate)
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


    public enum SearcherDefinition
    {
        EXACT_TEXT(com.atlassian.jira.issue.customfields.searchers.ExactTextSearcher.class,
                "templates/field/searcher/view-searcher-basictext.vm",
                "templates/field/searcher/search-basictext.vm"),
        LIKE_TEXT(com.atlassian.jira.issue.customfields.searchers.TextSearcher.class,
                "templates/field/searcher/view-searcher-basictext.vm",
                "templates/field/searcher/search-basictext.vm");

        private final Class searcherClassFullyQualifiedName;
        private final String viewTemplate;
        private final String searchTemplate;

        SearcherDefinition(final Class searcherClassFullyQualifiedName, final String viewTemplate, final String searchTemplate)
        {
            this.searcherClassFullyQualifiedName = searcherClassFullyQualifiedName;
            this.viewTemplate = viewTemplate;
            this.searchTemplate = searchTemplate;
        }

        public String getSearcherClassFullyQualifiedName()
        {
            return searcherClassFullyQualifiedName.getCanonicalName();
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
