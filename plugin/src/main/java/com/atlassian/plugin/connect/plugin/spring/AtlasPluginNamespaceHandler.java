package com.atlassian.plugin.connect.plugin.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class AtlasPluginNamespaceHandler extends NamespaceHandlerSupport
{

    @Override
    public void init()
    {
        registerBeanDefinitionParser("component-scan",new AtlasPluginComponentScanBeanDefinitionParser());
    }
}
