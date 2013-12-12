package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaType;

public class StringSchema extends SimpleTypeSchema
{
    private String pattern;
    private Integer maxLength;
    private Integer minLength;
    private String format;

    public StringSchema()
    {
        super(SchemaType.STRING.name().toLowerCase());
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public Integer getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength)
    {
        this.maxLength = maxLength;
    }

    public Integer getMinLength()
    {
        return minLength;
    }

    public void setMinLength(Integer minLength)
    {
        this.minLength = minLength;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }
}
