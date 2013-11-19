package com.atlassian.json.schema.doclet.model;

public class SchemaFieldDoc
{
    private String fieldName;
    private String fieldTitle;
    private String fieldDocs;

    public SchemaFieldDoc()
    {
        this.fieldName = "";
        this.fieldTitle = "";
        this.fieldDocs = "";
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldTitle()
    {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle)
    {
        this.fieldTitle = fieldTitle;
    }

    public String getFieldDocs()
    {
        return fieldDocs;
    }

    public void setFieldDocs(String fieldDocs)
    {
        this.fieldDocs = fieldDocs;
    }
}
