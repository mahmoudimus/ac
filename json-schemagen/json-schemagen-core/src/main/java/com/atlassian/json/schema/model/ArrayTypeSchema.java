package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaType;

public class ArrayTypeSchema extends BasicSchema
{
    private JsonSchema items;
    private Integer maxItems;
    private Integer minItems;
    private Boolean additionalItems;
    private Boolean uniqueItems;

    public ArrayTypeSchema()
    {
        setType(SchemaType.ARRAY.name().toLowerCase());
    }

    public void setItems(JsonSchema items)
    {
        this.items = items;
    }

    public Integer getMaxItems()
    {
        return maxItems;
    }

    public void setMaxItems(Integer maxItems)
    {
        this.maxItems = maxItems;
    }

    public Integer getMinItems()
    {
        return minItems;
    }

    public void setMinItems(Integer minItems)
    {
        this.minItems = minItems;
    }

    public Boolean getAdditionalItems()
    {
        return additionalItems;
    }

    public void setAdditionalItems(Boolean additionalItems)
    {
        this.additionalItems = additionalItems;
    }

    public Boolean getUniqueItems()
    {
        return uniqueItems;
    }

    public void setUniqueItems(Boolean uniqueItems)
    {
        this.uniqueItems = uniqueItems;
    }
}
