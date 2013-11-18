package com.atlassian.json.schema.model;

public interface JsonSchema
{
    String getId();
    String getType();
    String getRef();
    String getTitle();
    String getDescription();
    void setTitle(String title);
    void setDescription(String description);
}
