package com.atlassian.plugin.connect.plugin.web.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.web.condition.ConnectCondition;
import com.atlassian.plugin.connect.api.web.condition.ConnectConditionContext;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;

import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.modules.util.ConditionUtils.isRemoteCondition;

@Component
public class ConditionModuleFragmentFactoryImpl implements ConditionModuleFragmentFactory {
    private static final Logger log = LoggerFactory.getLogger(ConditionModuleFragmentFactory.class);
    private static final String TYPE_KEY = "type";

    private ConditionClassAccessor conditionClassAccessor;

    @Autowired
    public ConditionModuleFragmentFactoryImpl(ConditionClassAccessor conditionClassAccessor) {
        this.conditionClassAccessor = conditionClassAccessor;
    }

    @Override
    public DOMElement createFragment(String addonKey, List<ConditionalBean> beans) {
        return createFragment(addonKey, beans, Collections.<Class<? extends Condition>>emptyList());
    }

    @Override
    public DOMElement createFragment(String addonKey, List<ConditionalBean> beans,
                                     Iterable<Class<? extends Condition>> additionalStaticConditions) {
        DOMElement element = new DOMElement("conditions");
        element.addAttribute(TYPE_KEY, "AND");

        processConditionBeans(addonKey, beans).forEach(element::add);

        for (Class<? extends Condition> conditionClass : additionalStaticConditions) {
            element.add(createSingleCondition(conditionClass));
        }

        return element;

    }

    @Override
    public Map<String, String> getFragmentParameters(String addonKey, SingleConditionBean conditionBean) {
        ConnectConditionContext.Builder contextBuilder = ConnectConditionContext.builder(conditionBean.getParams());

        if (isRemoteCondition(conditionBean)) {
            contextBuilder.put(AddonCondition.ADDON_KEY, addonKey);
            contextBuilder.put(AddonCondition.URL, conditionBean.getCondition());
        } else {
            Optional<Class<? extends Condition>> optionalConditionClass = getConditionClass(conditionBean);
            optionalConditionClass.ifPresent(conditionClass -> {
                if (conditionClass.isAnnotationPresent(ConnectCondition.class)) {
                    contextBuilder.putAddonKey(addonKey);
                }
            });
        }

        return contextBuilder.build().toMap();
    }

    private List<DOMElement> processConditionBeans(String pluginKey, List<ConditionalBean> beans) {
        List<DOMElement> elements = new ArrayList<DOMElement>();

        for (ConditionalBean bean : beans) {
            if (SingleConditionBean.class.isAssignableFrom(bean.getClass())) {
                DOMElement element = createSingleCondition(pluginKey, (SingleConditionBean) bean);

                if (null != element) {
                    elements.add(element);
                }
            } else if (CompositeConditionBean.class.isAssignableFrom(bean.getClass())) {
                DOMElement composite = new DOMElement("conditions");
                CompositeConditionBean ccb = (CompositeConditionBean) bean;

                composite.addAttribute(TYPE_KEY, ccb.getType().toString().toUpperCase());

                processConditionBeans(pluginKey, ccb.getConditions()).forEach(composite::add);

                elements.add(composite);
            }
        }

        return elements;
    }

    private DOMElement createSingleCondition(String addonKey, SingleConditionBean conditionBean) {
        DOMElement element = null;

        Optional<Class<? extends Condition>> optionalConditionClass = getConditionClass(conditionBean);
        if (optionalConditionClass.isPresent()) {
            Class<? extends Condition> conditionClass = optionalConditionClass.get();
            Map<String, String> conditionElementParameters = getFragmentParameters(addonKey, conditionBean);

            element = new DOMElement("condition");
            element.addAttribute("class", conditionClass.getName());
            element.addAttribute("invert", Boolean.toString(conditionBean.isInvert()));

            for (Map.Entry<String, String> entry : conditionElementParameters.entrySet()) {
                element.addElement("param")
                        .addAttribute("name", entry.getKey())
                        .addAttribute("value", entry.getValue());
            }
        } else {
            log.warn("Condition with name " + conditionBean.getCondition() + " could not be found");
        }

        return element;
    }

    private Optional<Class<? extends Condition>> getConditionClass(SingleConditionBean conditionBean) {
        Optional<Class<? extends Condition>> optionalConditionClass;
        if (isRemoteCondition(conditionBean)) {
            optionalConditionClass = Optional.of(AddonCondition.class);
        } else {
            optionalConditionClass = conditionClassAccessor.getConditionClassForHostContext(conditionBean);
        }
        return optionalConditionClass;
    }

    private DOMElement createSingleCondition(Class<? extends Condition> conditionClass) {
        DOMElement element = new DOMElement("condition");
        element.addAttribute("class", conditionClass.getName());
        return element;
    }
}
