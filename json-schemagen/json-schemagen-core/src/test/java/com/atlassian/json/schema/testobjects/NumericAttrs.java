package com.atlassian.json.schema.testobjects;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.NumericSchemaAttributes;

public class NumericAttrs
{
    @CommonSchemaAttributes(title = "Integery")
    @NumericSchemaAttributes(multipleOf = 20, maximum = 1000, minimum = 100, exclusiveMinimum = true)
    private Integer myInteger;

    @CommonSchemaAttributes(title = "Numbery")
    @NumericSchemaAttributes(multipleOf = 2.5, maximum = 1000.5, minimum = 10, exclusiveMaximum = true)
    private Double myNumber;
}
