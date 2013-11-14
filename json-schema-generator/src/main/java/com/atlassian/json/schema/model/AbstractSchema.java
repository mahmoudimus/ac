package com.atlassian.json.schema.model;

public class AbstractSchema implements JsonSchema
{
    private String id;
    private String $ref;
    private String type;

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
}
