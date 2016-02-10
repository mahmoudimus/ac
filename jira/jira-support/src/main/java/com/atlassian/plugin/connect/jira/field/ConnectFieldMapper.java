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
                .put(STRING, mapping(ConnectFieldBaseTypeDefinition.TEXT, ConnectFieldSearcherDefinition.EXACT_TEXT))
                .put(TEXT, mapping(ConnectFieldBaseTypeDefinition.TEXT, ConnectFieldSearcherDefinition.LIKE_TEXT))
                .build();
    }

    public ConnectFieldTypeDefinition getMapping(IssueFieldType fieldType)
    {
        return map.get(fieldType);
    }

    public static class ConnectFieldTypeDefinition
    {
        ConnectFieldBaseTypeDefinition typeDefinition;
        ConnectFieldSearcherDefinition searcherDefinition;

        public ConnectFieldTypeDefinition(final ConnectFieldBaseTypeDefinition typeDefinition, final ConnectFieldSearcherDefinition searcherDefinition)
        {
            this.typeDefinition = typeDefinition;
            this.searcherDefinition = searcherDefinition;
        }

        public static ConnectFieldTypeDefinition mapping(final ConnectFieldBaseTypeDefinition typeDefinition, final ConnectFieldSearcherDefinition searcherDefinition)
        {
            return new ConnectFieldTypeDefinition(typeDefinition, searcherDefinition);
        }

        public ConnectFieldBaseTypeDefinition getType()
        {
            return typeDefinition;
        }

        public ConnectFieldSearcherDefinition getSearcherBase()
        {
            return searcherDefinition;
        }
    }

    public enum ConnectFieldBaseTypeDefinition
    {
        TEXT(com.atlassian.jira.issue.customfields.impl.GenericTextCFType.class,
                "templates/field/text/view-basictext.vm",
                "templates/field/text/edit-maxlengthtext.vm",
                "templates/field/text/xml-basictext.vm");

        private final Class baseCFTypeClass;
        private final String viewTemplate;
        private final String editTemplate;
        private final String xmlTemplate;

        ConnectFieldBaseTypeDefinition(final Class baseCFTypeClass, final String viewTemplate, final String editTemplate, final String xmlTemplate)
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


    public enum ConnectFieldSearcherDefinition
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

        ConnectFieldSearcherDefinition(final Class searcherClassFullyQualifiedName, final String viewTemplate, final String searchTemplate)
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
