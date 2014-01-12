package com.atlassian.json.schema.model;

import java.util.Set;

public class AbstractSchema implements JsonSchema
{
    private String id;
    private String $ref;
    private String type;
    private String title;
    private String description;
    private String fieldTitle;
    private String fieldDescription;
    private Set<ObjectSchema> allOf;
    private Set<ObjectSchema> anyOf;
    private Set<ObjectSchema> oneOf;
    private ObjectSchema not;
    private Set<ObjectSchema> definitions;
    private String defaultValue;
    
    @Override
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String getRef()
    {
        return $ref;
    }

    public void setRef(String $ref)
    {
        this.$ref = $ref;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getFieldTitle()
    {
        return fieldTitle;
    }

    @Override
    public String getFieldDescription()
    {
        return fieldDescription;
    }

    @Override
    public void setFieldTitle(String title)
    {
        this.fieldTitle = title;
    }

    @Override
    public void setFieldDescription(String description)
    {
        this.fieldDescription = description;
    }

    @Override
    public Set<ObjectSchema> getAllOf()
    {
        return allOf;
    }

    public void setAllOf(Set<ObjectSchema> allOf)
    {
        this.allOf = allOf;
    }

    @Override
    public Set<ObjectSchema> getAnyOf()
    {
        return anyOf;
    }

    public void setAnyOf(Set<ObjectSchema> anyOf)
    {
        this.anyOf = anyOf;
    }

    @Override
    public Set<ObjectSchema> getOneOf()
    {
        return oneOf;
    }

    public void setOneOf(Set<ObjectSchema> oneOf)
    {
        this.oneOf = oneOf;
    }

    @Override
    public ObjectSchema getNot()
    {
        return not;
    }

    public void setNot(ObjectSchema not)
    {
        this.not = not;
    }

    @Override
    public Set<ObjectSchema> getDefinitions()
    {
        return definitions;
    }

    public void setDefinitions(Set<ObjectSchema> definitions)
    {
        this.definitions = definitions;
    }

    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }
}
