package com.atlassian.plugin.connect.plugin.web;

import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParamsModuleFragmentFactoryImpl implements ParamsModuleFragmentFactory
{
    public void addParamsToElement(Element element, Map<String, String> params)
    {
        for(Map.Entry<String,String> entry : params.entrySet())
        {
            element.addElement("param")
                   .addAttribute("name",entry.getKey())
                   .addAttribute("value",entry.getValue());
        }
    }
}
