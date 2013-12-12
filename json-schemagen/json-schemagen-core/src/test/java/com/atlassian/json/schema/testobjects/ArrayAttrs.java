package com.atlassian.json.schema.testobjects;

import com.atlassian.json.schema.annotation.ArraySchemaAttributes;
import com.atlassian.json.schema.annotation.CommonSchemaAttributes;

import java.util.List;

public class ArrayAttrs
{
    @CommonSchemaAttributes(title = "smelly", defaultValue = "yuck")
    @ArraySchemaAttributes(maxItems = 50, minItems = 2, additionalItems = false)
    List<SimpleSmell> smells;
}
