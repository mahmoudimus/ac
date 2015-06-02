package com.atlassian.plugin.connect.spi.module.provider;

import org.springframework.beans.factory.annotation.Autowired;

public class ModuleListProviderContainer
{
    private int test;
    
    @Autowired
    public ModuleListProviderContainer()
    {
        test = 3;
        
    }
}
