package com.atlassian.json.schema.scanner.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterfaceList
{
    private List<InterfaceImplementors> interfaceList;

    public InterfaceList()
    {
        this.interfaceList = new ArrayList<InterfaceImplementors>();
    }

    public InterfaceList(List<InterfaceImplementors> interfaceList)
    {
        this.interfaceList = interfaceList;
    }

    public List<InterfaceImplementors> getInterfaceList()
    {
        return interfaceList;
    }
    
    public List<String> getImplementors(Class iface)
    {
        for(InterfaceImplementors implList : interfaceList)
        {
            if(iface.getName().equals(implList.getInterfaceName()))
            {
                return implList.getImplementors();
            }
        }
        
        return Collections.EMPTY_LIST;
    }
}
