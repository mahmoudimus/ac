package com.atlassian.json.schema.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.json.schema.SchemaTypes;

import com.google.common.base.CaseFormat;

public class ObjectSchema extends RootSchema
{
    private Map<String,Object> properties;
    private List<String> required;
    
    public ObjectSchema()
    {
        setType(SchemaTypes.OBJECT);
    }
    
    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public List<String> getRequired()
    {
        return required;
    }

    public void setRequired(List<String> required)
    {
        this.required = required;
    }
}
