package com.atlassian.json.schema.model;

import java.util.List;

public class EnumSchema<T> extends BasicSchema
{
    private List<T> enumList;

    public void setEnumList(List<T> enumList)
    {
        this.enumList = enumList;
    }
}
