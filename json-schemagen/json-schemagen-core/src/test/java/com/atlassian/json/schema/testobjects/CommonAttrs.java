package com.atlassian.json.schema.testobjects;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;

public class CommonAttrs
{
    @CommonSchemaAttributes(title = "Color me Bad"
        ,description = "Black and White"
        ,defaultValue = "red"
    )
    private String color;

    @CommonSchemaAttributes(allOf = {SimpleSmell.class,FoodObject.class})
    private String grownIn;

    @CommonSchemaAttributes(anyOf = {SimpleSmell.class})
    private String smellsGood;

    @CommonSchemaAttributes(not = SimpleSmell.class)
    private String noSmell;

}
