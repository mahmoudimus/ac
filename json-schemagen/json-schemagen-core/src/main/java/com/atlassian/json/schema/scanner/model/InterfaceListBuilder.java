package com.atlassian.json.schema.scanner.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class InterfaceListBuilder
{
    private final Map<String,List<String>> implementors;

    public InterfaceListBuilder()
    {
        this.implementors = new HashMap<String, List<String>>();
    }
    
    public InterfaceListBuilder withImplementation(Class implementation)
    {
        for(Class iface : implementation.getInterfaces())
        {
            if(!implementors.containsKey(iface.getName()))
            {
                implementors.put(iface.getName(),newArrayList(implementation.getName()));
            }
            else
            {
                implementors.get(iface.getName()).add(implementation.getName());
            }
        }
        
        return this;
    }
    
    public InterfaceList build()
    {
        List<InterfaceImplementors> implList = new ArrayList<InterfaceImplementors>();
        
        for(Map.Entry<String,List<String>> entry : implementors.entrySet())
        {
            implList.add(new InterfaceImplementors(entry.getKey(),entry.getValue()));
        }
        
        return new InterfaceList(implList);
    }
}
