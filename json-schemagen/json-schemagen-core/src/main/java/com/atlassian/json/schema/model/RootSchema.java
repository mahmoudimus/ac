package com.atlassian.json.schema.model;

public class RootSchema extends AbstractSchema
{
    public static final String DRAFTV4 = "http://json-schema.org/draft-04/schema#";
    
    private String $schema;

    public String getDollarSchema()
    {
        return $schema;
    }

    public void addDollarSchema()
    {
        this.$schema = DRAFTV4;
    }
}
