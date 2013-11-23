package com.atlassian.json.schema.testobjects;

import java.util.List;
import java.util.Map;

public class FoodObject
{
    private String color;
    private String name;
    private List<String> grownIn;
    private Map<String,String> params;
    private FoodType foodType;
    private SoldAs soldAs;
    private List<Smell> smells;

    public void setColor(String color)
    {
        this.color = color;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
