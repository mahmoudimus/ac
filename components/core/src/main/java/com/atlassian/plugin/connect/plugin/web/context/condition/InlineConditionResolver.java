package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.web.condition.PluggableConditionClassAccessor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InlineConditionResolver
{
    private static final Logger log = LoggerFactory.getLogger(InlineConditionResolver.class);

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
        try
        {
            return conditionClassAccessor.getConditionClassForInline(toConditionBean(inlineCondition))
                    .map(conditionClass -> createAndInitCondition(conditionClass, inlineCondition.getParams()))
                    .map(condition -> condition.shouldDisplay(context));
        }
        catch (RuntimeException exception)
        {
            log.warn(String.format("'%s' inline condition has thrown an exception. Context: %s", inlineCondition, context), exception);
            return Optional.empty();
        }
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
