package com.atlassian.json.schema.testobjects;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;

public class StringAttrs
{
    @CommonSchemaAttributes(title = "Stringy")
    @StringSchemaAttributes(maxLength = 100, minLength = 1, format = "url", pattern = "regexGoesHere")
    private String myString;
    
}
