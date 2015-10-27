package com.atlassian.plugin.connect.plugin.web;

import org.dom4j.Element;

import java.util.Map;

public interface ParamsModuleFragmentFactory
{
    public void addParamsToElement(Element element, Map<String, String> params);
}
