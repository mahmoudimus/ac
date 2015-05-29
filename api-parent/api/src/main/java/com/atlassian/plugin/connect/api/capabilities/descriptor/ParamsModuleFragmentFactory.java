package com.atlassian.plugin.connect.api.capabilities.descriptor;

import org.dom4j.Element;

import java.util.Map;

public interface ParamsModuleFragmentFactory
{
    public void addParamsToElement(Element element, Map<String, String> params);
}
