package com.atlassian.json.schema.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class EnumSchema<T> extends RootSchema
{
    @SerializedName("enum")
    private List<T> enumList;

    public void setEnumList(List<T> enumList)
    {
        this.enumList = enumList;
    }
}
