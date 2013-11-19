package com.atlassian.json.schema.testobjects;

public enum SoldAs
{
    INDIVIDUAL(1), BUNCH(5);

    private int num;
    
    SoldAs(int num)
    {
        this.num = num;
    }


    @Override
    public String toString()
    {
        return Integer.toString(num);
    }
}
