package com.atlassian.plugin.connect.jira.field;

import com.atlassian.plugin.connect.modules.beans.IssueFieldType;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.plugin.connect.jira.field.IssueFieldMapper.IssueFieldTypeDefinition.mapping;
import static com.atlassian.plugin.connect.modules.beans.IssueFieldType.STRING;
import static com.atlassian.plugin.connect.modules.beans.IssueFieldType.TEXT;

@JiraComponent
public class IssueFieldMapper
{
    private final ImmutableMap<IssueFieldType, IssueFieldTypeDefinition> map;

    public IssueFieldMapper()
    {
        map = ImmutableMap.<IssueFieldType,IssueFieldTypeDefinition>builder()
                .put(STRING, mapping(IssueFieldBaseTypeDefinition.TEXT, IssueFieldSearcherDefinition.EXACT_TEXT))
                .put(TEXT, mapping(IssueFieldBaseTypeDefinition.TEXT, IssueFieldSearcherDefinition.LIKE_TEXT))
                .build();
    }

    public IssueFieldTypeDefinition getMapping(IssueFieldType fieldType)
    {
        return map.get(fieldType);
    }

    public static class IssueFieldTypeDefinition
    {
        IssueFieldBaseTypeDefinition typeDefinition;
        IssueFieldSearcherDefinition searcherDefinition;

        public IssueFieldTypeDefinition(final IssueFieldBaseTypeDefinition typeDefinition, final IssueFieldSearcherDefinition searcherDefinition)
        {
            this.typeDefinition = typeDefinition;
            this.searcherDefinition = searcherDefinition;
        }

        public static IssueFieldTypeDefinition mapping(final IssueFieldBaseTypeDefinition typeDefinition, final IssueFieldSearcherDefinition searcherDefinition)
        {
            return new IssueFieldTypeDefinition(typeDefinition, searcherDefinition);
        }

        public IssueFieldBaseTypeDefinition getType()
        {
            return typeDefinition;
        }

        public IssueFieldSearcherDefinition getSearcherBase()
        {
            return searcherDefinition;
        }
    }

    public enum IssueFieldBaseTypeDefinition
    {
        TEXT(com.atlassian.jira.issue.customfields.impl.GenericTextCFType.class,
                "templates/field/text/view-basictext.vm",
                "templates/field/text/edit-maxlengthtext.vm",
                "templates/field/text/xml-basictext.vm");

        private final Class baseCFTypeClass;
        private final String viewTemplate;
        private final String editTemplate;
        private final String xmlTemplate;

        IssueFieldBaseTypeDefinition(final Class baseCFTypeClass, final String viewTemplate, final String editTemplate, final String xmlTemplate)
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


    public enum IssueFieldSearcherDefinition
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

        IssueFieldSearcherDefinition(final Class searcherClassFullyQualifiedName, final String viewTemplate, final String searchTemplate)
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
