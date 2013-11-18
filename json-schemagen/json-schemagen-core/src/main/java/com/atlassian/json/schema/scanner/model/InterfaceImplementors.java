package com.atlassian.json.schema.scanner.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterfaceImplementors
{
    private String interfaceName;
    private Set<String> implementors;

    public InterfaceImplementors()
    {
        this.implementors = new HashSet<String>();
    }
    
    public InterfaceImplementors(String interfaceName, Set<String> implementors)
    {
        this.interfaceName = interfaceName;
        this.implementors = implementors;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    public Set<String> getImplementors()
    {
        return implementors;
    }
}
