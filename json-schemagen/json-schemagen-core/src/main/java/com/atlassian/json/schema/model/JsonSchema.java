package com.atlassian.json.schema.model;

import java.util.Set;

public interface JsonSchema
{
    String getId();
    String getType();
    String getRef();
    String getTitle();
    String getDescription();
    void setTitle(String title);
    void setDescription(String description);
    Set<ObjectSchema> getAllOf();
    Set<ObjectSchema> getAnyOf();
    Set<ObjectSchema> getOneOf();
    ObjectSchema getNot();
    Set<ObjectSchema> getDefinitions();
    String getDefaultValue();

}
