package com.atlassian.json.schema.doclet.model;

import java.util.Collections;
import java.util.List;

public class JsonSchemaDocs
{
    private List<SchemaClassDoc> classDocs;

    public JsonSchemaDocs()
    {
        this.classDocs = Collections.EMPTY_LIST;
    }

    public List<SchemaClassDoc> getClassDocs()
    {
        return classDocs;
    }

    public void setClassDocs(List<SchemaClassDoc> classDocs)
    {
        this.classDocs = classDocs;
    }
    
    public SchemaClassDoc getClassDoc(String className)
    {
        for(SchemaClassDoc doc : classDocs)
        {
            if(className.equals(doc.getClassName()))
            {
                return doc;
            }
        }
        
        return null;
    }
}
