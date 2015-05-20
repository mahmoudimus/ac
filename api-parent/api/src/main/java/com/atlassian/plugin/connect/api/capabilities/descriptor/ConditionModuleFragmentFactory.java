package com.atlassian.plugin.connect.api.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.web.Condition;
import org.dom4j.dom.DOMElement;

import java.util.List;

public interface ConditionModuleFragmentFactory
{
    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans);

    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans,
            Iterable<Class<? extends Condition>> additionalStaticConditions);
}
