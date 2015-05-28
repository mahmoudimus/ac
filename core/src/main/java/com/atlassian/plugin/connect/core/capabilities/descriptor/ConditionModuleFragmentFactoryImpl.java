package com.atlassian.plugin.connect.core.capabilities.descriptor;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.api.capabilities.descriptor.ParamsModuleFragmentFactory;
import com.atlassian.plugin.connect.core.capabilities.module.AddOnCondition;
import com.atlassian.plugin.connect.core.condition.ConnectCondition;
import com.atlassian.plugin.connect.core.condition.ConnectConditionContext;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ConditionUtils.isRemoteCondition;

@Component
public class ConditionModuleFragmentFactoryImpl implements ConditionModuleFragmentFactory
{
    private static final Logger log = Logger.getLogger(ConditionModuleFragmentFactory.class);
    private static final String TYPE_KEY = "type";

    private final ProductAccessor productAccessor;
    private final ParamsModuleFragmentFactory paramsModuleFragmentFactory;

    @Autowired
    public ConditionModuleFragmentFactoryImpl(ProductAccessor productAccessor,
            ParamsModuleFragmentFactory paramsModuleFragmentFactory)
    {
        this.productAccessor = productAccessor;
        this.paramsModuleFragmentFactory = paramsModuleFragmentFactory;
    }

    @Override
    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans)
    {
        return createFragment(pluginKey, beans, Collections.<Class<? extends Condition>>emptyList());
    }

    @Override
    public DOMElement createFragment(String pluginKey, List<ConditionalBean> beans,
            Iterable<Class<? extends Condition>> additionalStaticConditions)
    {
        DOMElement element = new DOMElement("conditions");
        element.addAttribute(TYPE_KEY, "AND");

        List<DOMElement> conditions = processConditionBeans(pluginKey, beans);

        for (DOMElement condition : conditions)
        {
            element.add(condition);
        }

        for (Class<? extends Condition> conditionClass : additionalStaticConditions)
        {
            element.add(createSingleCondition(conditionClass));
        }

        return element;

    }

    private List<DOMElement> processConditionBeans(String pluginKey, List<ConditionalBean> beans)
    {
        List<DOMElement> elements = new ArrayList<DOMElement>();

        for (ConditionalBean bean : beans)
        {
            if (SingleConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                DOMElement element = createSingleCondition(pluginKey, (SingleConditionBean) bean);

                if (null != element)
                {
                    elements.add(element);
                }
            }
            else if (CompositeConditionBean.class.isAssignableFrom(bean.getClass()))
            {
                DOMElement composite = new DOMElement("conditions");
                CompositeConditionBean ccb = (CompositeConditionBean) bean;

                composite.addAttribute(TYPE_KEY, ccb.getType().toString().toUpperCase());

                List<DOMElement> subConditions = processConditionBeans(pluginKey, ccb.getConditions());

                for (DOMElement subcondition : subConditions)
                {
                    composite.add(subcondition);
                }

                elements.add(composite);
            }
        }

        return elements;
    }


    private DOMElement createSingleCondition(String addOnKey, SingleConditionBean bean)
    {
        String className = "";
        DOMElement element = null;

        final ConnectConditionContext.Builder contextBuilder = ConnectConditionContext.builder(bean.getParams());

        if (isRemoteCondition(bean))
        {
            className = AddOnCondition.class.getName();
            contextBuilder.put(AddOnCondition.ADDON_KEY, addOnKey);
            contextBuilder.put(AddOnCondition.URL, bean.getCondition());
        }
        else
        {
            ConditionClassResolver conditionClassResolver = productAccessor.getConditions();
            Option<? extends Class<? extends Condition>> clazz = conditionClassResolver.get(bean.getCondition(), bean.getParams());

            if (clazz.isDefined())
            {
                className = clazz.get().getName();
                if (clazz.get().isAnnotationPresent(ConnectCondition.class))
                {
                    contextBuilder.putAddOnKey(addOnKey);
                }
            }
        }

        if (!Strings.isNullOrEmpty(className))
        {
            element = new DOMElement("condition");
            element.addAttribute("class", className);
            element.addAttribute("invert", Boolean.toString(bean.isInvert()));

            paramsModuleFragmentFactory.addParamsToElement(element, contextBuilder.build().toMap());
        }
        else
        {
            log.warn("Condition with name " + bean.getCondition() + " could not be found");
        }

        return element;
    }

    private DOMElement createSingleCondition(Class<? extends Condition> conditionClass)
    {
        DOMElement element = new DOMElement("condition");
        element.addAttribute("class", conditionClass.getName());
        return element;
    }

}
