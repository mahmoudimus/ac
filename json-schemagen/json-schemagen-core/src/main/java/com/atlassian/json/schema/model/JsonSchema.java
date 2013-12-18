package com.atlassian.json.schema.model;

import java.util.Set;

public interface JsonSchema
{
    String getId();
    String getType();
    String getRef();
    String getTitle();
    String getDescription();
    String getFieldTitle();
    String getFieldDescription();
    void setTitle(String title);
    void setDescription(String description);
    void setFieldTitle(String title);
    void setFieldDescription(String description);
    Set<ObjectSchema> getAllOf();
    Set<ObjectSchema> getAnyOf();
    Set<ObjectSchema> getOneOf();
    ObjectSchema getNot();
    Set<ObjectSchema> getDefinitions();
    String getDefaultValue();

}
