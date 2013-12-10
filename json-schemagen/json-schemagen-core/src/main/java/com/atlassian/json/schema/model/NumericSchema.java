package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaType;

public class NumericSchema extends SimpleTypeSchema
{
    private Double multipleOf;
    private Double maximum;
    private Double minimum;
    private Boolean exclusiveMaximum;
    private Boolean exclusiveMinimum;
    
    public NumericSchema(String type)
    {
        super(type);
    }

    public Double getMultipleOf()
    {
        return multipleOf;
    }

    public void setMultipleOf(Double multipleOf)
    {
        this.multipleOf = multipleOf;
    }

    public Double getMaximum()
    {
        return maximum;
    }

    public void setMaximum(Double maximum)
    {
        this.maximum = maximum;
    }

    public Double getMinimum()
    {
        return minimum;
    }

    public void setMinimum(Double minimum)
    {
        this.minimum = minimum;
    }

    public Boolean getExclusiveMaximum()
    {
        return exclusiveMaximum;
    }

    public void setExclusiveMaximum(Boolean exclusiveMaximum)
    {
        this.exclusiveMaximum = exclusiveMaximum;
    }

    public Boolean getExclusiveMinimum()
    {
        return exclusiveMinimum;
    }

    public void setExclusiveMinimum(Boolean exclusiveMinimum)
    {
        this.exclusiveMinimum = exclusiveMinimum;
    }
}
