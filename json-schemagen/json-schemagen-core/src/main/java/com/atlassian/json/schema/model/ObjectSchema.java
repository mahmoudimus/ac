package com.atlassian.json.schema.model;

import com.atlassian.json.schema.SchemaType;

import java.util.Map;
import java.util.Set;

public class ObjectSchema extends BasicSchema
{
    private Map<String, Object> properties;
    private Set<String> required;
    private Integer maxProperties;
    private Integer minProperties;
    private Boolean additionalProperties;
    private Set<String> patternProperties;
    private Map<String,Set<String>> dependencies;

    public ObjectSchema()
    {
        setType(SchemaType.OBJECT.name().toLowerCase());
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public Set<String> getRequired()
    {
        return required;
    }

    public void setRequired(Set<String> required)
    {
        this.required = required;
    }

    public Integer getMaxProperties()
    {
        return maxProperties;
    }

    public void setMaxProperties(Integer maxProperties)
    {
        this.maxProperties = maxProperties;
    }

    public Integer getMinProperties()
    {
        return minProperties;
    }

    public void setMinProperties(Integer minProperties)
    {
        this.minProperties = minProperties;
    }

    public Boolean getAdditionalProperties()
    {
        return additionalProperties;
    }

    public void setAdditionalProperties(Boolean additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

    public Set<String> getPatternProperties()
    {
        return patternProperties;
    }

    public void setPatternProperties(Set<String> patternProperties)
    {
        this.patternProperties = patternProperties;
    }

    public Map<String, Set<String>> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(Map<String, Set<String>> dependencies)
    {
        this.dependencies = dependencies;
    }
}
