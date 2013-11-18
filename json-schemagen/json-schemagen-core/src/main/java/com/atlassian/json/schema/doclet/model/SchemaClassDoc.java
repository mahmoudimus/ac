package com.atlassian.json.schema.doclet.model;

import java.util.Collections;
import java.util.List;

public class SchemaClassDoc
{
    private String className;
    private String classTitle;
    private String classDoc;
    
    private List<SchemaFieldDoc> fieldDocs;

    public SchemaClassDoc()
    {
        this.className = "";
        this.classTitle = "";
        this.classDoc = "";
        this.fieldDocs = Collections.EMPTY_LIST;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public List<SchemaFieldDoc> getFieldDocs()
    {
        return fieldDocs;
    }

    public void setFieldDocs(List<SchemaFieldDoc> fieldDocs)
    {
        this.fieldDocs = fieldDocs;
    }

    public String getClassTitle()
    {
        return classTitle;
    }

    public void setClassTitle(String classTitle)
    {
        this.classTitle = classTitle;
    }

    public String getClassDoc()
    {
        return classDoc;
    }

    public void setClassDoc(String classDoc)
    {
        this.classDoc = classDoc;
    }
    
    public SchemaFieldDoc getFieldDoc(String fieldName)
    {
        for(SchemaFieldDoc fieldDoc : fieldDocs)
        {
            if(fieldName.equals(fieldDoc.getFieldName()))
            {
                return fieldDoc;
            }
        }
        
        return null;
    }
}
