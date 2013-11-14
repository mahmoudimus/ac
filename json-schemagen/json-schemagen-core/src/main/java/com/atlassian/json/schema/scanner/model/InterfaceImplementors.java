package com.atlassian.json.schema.scanner.model;

import java.util.ArrayList;
import java.util.List;

public class InterfaceImplementors
{
    private String interfaceName;
    private List<String> implementors;

    public InterfaceImplementors()
    {
        this.implementors = new ArrayList<String>();
    }
    
    public InterfaceImplementors(String interfaceName, List<String> implementors)
    {
        this.interfaceName = interfaceName;
        this.implementors = implementors;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    public List<String> getImplementors()
    {
        return implementors;
    }
}
