package com.atlassian.json.schema.model;

public class AbstractSchema implements JsonSchema
{
    private String id;
    private String $ref;
    private String type;
    private String title;
    private String description;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getRef()
    {
        return $ref;
    }

    public void setRef(String $ref)
    {
        this.$ref = $ref;
    }

    public String get$ref()
    {
        return $ref;
    }

    public void set$ref(String $ref)
    {
        this.$ref = $ref;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
