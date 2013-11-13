package com.atlassian.json.schema;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

public class JsonSchemaDoclet
{
    public static boolean start(RootDoc rootDoc)
    {
        for(ClassDoc classDoc : rootDoc.classes())
        {
            System.out.println(classDoc.typeName());
        }
        return true;
    }
}
