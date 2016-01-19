package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.web.condition.PluggableConditionClassAccessor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.web.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InlineConditionResolver
{
    private final PluggableConditionClassAccessor conditionClassAccessor;
    private final HostContainer hostContainer;

    @Autowired
    public InlineConditionResolver(final PluggableConditionClassAccessor conditionClassAccessor, final HostContainer hostContainer)
    {
        this.conditionClassAccessor = conditionClassAccessor;
        this.hostContainer = hostContainer;
    }

    public Optional<Boolean> resolve(InlineCondition inlineCondition, Map<String, Object> context)
    {
        return conditionClassAccessor.getConditionClassForHostContext(toConditionBean(inlineCondition))
                .map(conditionClass -> createAndInitCondition(conditionClass, inlineCondition.getParams()))
                .map(condition -> condition.shouldDisplay(context));
    }

    private SingleConditionBean toConditionBean(final InlineCondition condition)
    {
        SingleConditionBeanBuilder builder = new SingleConditionBeanBuilder().withCondition(condition.getConditionName());
        condition.getParams().entrySet().forEach(entry -> builder.withParam(entry.getKey(), entry.getValue()));
        return builder.build();
    }

    private Condition createAndInitCondition(final Class<? extends Condition> conditionClass, Map<String, String> params)
    {
        Condition condition = hostContainer.create(conditionClass);
        condition.init(params);
        return condition;
    }
}
