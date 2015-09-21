package com.atlassian.plugin.connect.api.capabilities.descriptor;

import com.atlassian.json.schema.DefaultJsonSchemaGenerator;
import com.atlassian.json.schema.EnumCase;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;

public class ConnectSchemaGenerator extends DefaultJsonSchemaGenerator
{

    public ConnectSchemaGenerator(EnumCase enumCase, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter)
    {
        super(enumCase, interfaceList, schemaDocs, ignoreFilter);
    }

    @Override
    protected String getFieldName(Field field)
    {
        String name = field.getName();
        
        if(field.isAnnotationPresent(SerializedName.class))
        {
            name = field.getAnnotation(SerializedName.class).value();
        }
        
        return name;
    }
}
