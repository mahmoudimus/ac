package com.atlassian.json.schema.testobjects;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDependency;

@ObjectSchemaAttributes(patternProperties = {"s","m_"}
        ,additionalProperties = true
        ,maxProperties = 5
        ,minProperties = 1
        ,dependencies = {
            @SchemaDependency(property = "smell", requires = "somethingElse")
        }
)
public class ObjectAttrs
{
    @CommonSchemaAttributes(title = "yummy")
    @Required
    private SimpleSmell mySmell;
    
    @Required
    private Integer height;
}
