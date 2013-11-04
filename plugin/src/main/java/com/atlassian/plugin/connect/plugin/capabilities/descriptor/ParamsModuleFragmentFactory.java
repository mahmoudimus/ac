package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.stereotype.Component;

@Component
public class ParamsModuleFragmentFactory
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
