package com.atlassian.json.schema.scanner.model;

import java.util.*;

public class InterfaceListBuilder
{
    private final Map<String, Set<String>> implementors;

    public InterfaceListBuilder()
    {
        this.implementors = new HashMap<String, Set<String>>();
    }

    public InterfaceListBuilder withImplementation(Class implementation)
    {
        for (Class iface : implementation.getInterfaces())
        {
            if (!implementors.containsKey(iface.getName()))
            {
                Set<String> impls = new HashSet<String>();
                impls.add(implementation.getName());

                implementors.put(iface.getName(), impls);
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

        for (Map.Entry<String, Set<String>> entry : implementors.entrySet())
        {
            implList.add(new InterfaceImplementors(entry.getKey(), entry.getValue()));
        }

        return new InterfaceList(implList);
    }
}
