package com.atlassian.json.schema.model;

public class AbstractSchema implements JsonSchema
{
    private String id;
    private String $ref;
    private String type;
    private String title;
    private String description;

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

}
