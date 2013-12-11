package com.atlassian.json.schema.testobjects;

import java.util.List;

import com.atlassian.json.schema.annotation.ArraySchemaAttributes;
import com.atlassian.json.schema.annotation.CommonSchemaAttributes;

public class ArrayAttrs
{
    @CommonSchemaAttributes(title = "smelly", defaultValue = "yuck")
    @ArraySchemaAttributes(maxItems = 50, minItems = 2, additionalItems = false)
    List<SimpleSmell> smells;
}
