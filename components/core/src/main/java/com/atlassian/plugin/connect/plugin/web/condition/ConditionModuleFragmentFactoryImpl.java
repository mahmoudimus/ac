package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Strings;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.atlassian.plugin.connect.modules.util.ConditionUtils.isRemoteCondition;

@Component
public class ConditionModuleFragmentFactoryImpl implements ConditionModuleFragmentFactory
{

    private static final Logger log = LoggerFactory.getLogger(ConditionModuleFragmentFactory.class);
    private static final String TYPE_KEY = "type";

    private PluginAccessor pluginAccessor;

    @Autowired
    public ConditionModuleFragmentFactoryImpl(PluginAccessor pluginAccessor, ProductAccessor productAccessor)
    {
        this.pluginAccessor = pluginAccessor;
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
            Optional<Class<? extends Condition>> optionalConditionClass = resolveConditionClassForHostContext(bean);

            if (optionalConditionClass.isPresent())
            {
                className = optionalConditionClass.get().getName();
                if (optionalConditionClass.get().isAnnotationPresent(ConnectCondition.class))
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

            for (Map.Entry<String,String> entry : contextBuilder.build().toMap().entrySet())
            {
                element.addElement("param")
                        .addAttribute("name",entry.getKey())
                        .addAttribute("value",entry.getValue());
            }
        }
        else
        {
            log.warn("Condition with name " + bean.getCondition() + " could not be found");
        }

        return element;
    }

    private Optional<Class<? extends Condition>> resolveConditionClassForHostContext(final SingleConditionBean bean)
    {
        Collection<ConnectConditionClassResolver> conditionClassResolvers = pluginAccessor.getEnabledModulesByClass(ConnectConditionClassResolver.class);
        return conditionClassResolvers.stream()
                .flatMap(new Function<ConnectConditionClassResolver, Stream<ConnectConditionClassResolver.Entry>>()
                {
                    @Override
                    public Stream<ConnectConditionClassResolver.Entry> apply(ConnectConditionClassResolver resolver)
                    {
                        return resolver.getEntries().stream();
                    }
                }).map(new Function<ConnectConditionClassResolver.Entry, Optional<Class<? extends Condition>>>()
                {
                    @Override
                    public Optional<Class<? extends Condition>> apply(ConnectConditionClassResolver.Entry resolverEntry)
                    {
                        return resolverEntry.getConditionClassForHostContext(bean);
                    }
                }).filter(new Predicate<Optional<Class<? extends Condition>>>()
                {
                    @Override
                    public boolean test(Optional<Class<? extends Condition>> optionalConditionClass)
                    {
                        return optionalConditionClass.isPresent();
                    }
                }).map(new Function<Optional<Class<? extends Condition>>, Class<? extends Condition>>()
                {
                    @Override
                    public Class<? extends Condition> apply(Optional<Class<? extends Condition>> optionalConditionClass)
                    {
                        return optionalConditionClass.get();
                    }
                }).findFirst();
    }

    private DOMElement createSingleCondition(Class<? extends Condition> conditionClass)
    {
        DOMElement element = new DOMElement("condition");
        element.addAttribute("class", conditionClass.getName());
        return element;
    }
}
