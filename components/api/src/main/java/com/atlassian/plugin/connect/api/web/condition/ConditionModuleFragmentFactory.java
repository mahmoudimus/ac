package com.atlassian.plugin.connect.api.web.condition;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;
import org.dom4j.dom.DOMElement;

import java.util.List;
import java.util.Map;

public interface ConditionModuleFragmentFactory {
    DOMElement createFragment(String pluginKey, List<ConditionalBean> beans);

    DOMElement createFragment(String pluginKey, List<ConditionalBean> beans,
                              Iterable<Class<? extends Condition>> additionalStaticConditions);

    Map<String, String> getFragmentParameters(String addonKey, SingleConditionBean conditionBean);
}
