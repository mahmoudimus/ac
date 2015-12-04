package com.atlassian.plugin.connect.plugin.web.redirect;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.web.condition.ConnectConditionFactory;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class RedirectDataBuilderBuilderFactoryImpl implements RedirectDataBuilderFactory
{
    private final ConnectConditionFactory connectConditionFactory;

    @Autowired
    public RedirectDataBuilderBuilderFactoryImpl(ConnectConditionFactory connectConditionFactory)
    {
        this.connectConditionFactory = connectConditionFactory;
    }

    public RedirectDataBuilder builder()
    {
        return new RedirectDataBuilderImpl(connectConditionFactory);
    }

    static class RedirectDataBuilderImpl
            implements RedirectDataBuilder, RedirectDataBuilder.AccessDeniedTemplateTypeBuilder,
            RedirectDataBuilder.ModuleUriBuilder, RedirectDataBuilder.InitializedBuilder
    {
        private final ConnectConditionFactory connectConditionFactory;

        private String addOnKey;
        private String urlTemplate;
        private String title;
        private final List<ConditionalBean> conditionalBeans = Lists.newArrayList();
        private RedirectData.AccessDeniedTemplateType accessDeniedTemplateType;

        RedirectDataBuilderImpl(ConnectConditionFactory connectConditionFactory)
        {
            this.connectConditionFactory = connectConditionFactory;
        }

        @Override
        public ModuleUriBuilder addOn(String key)
        {
            addOnKey = checkNotNull(key);
            return this;
        }

        @Override
        public InitializedBuilder conditions(Iterable<ConditionalBean> conditions)
        {
            Iterables.addAll(conditionalBeans, conditions);
            return this;
        }

        @Override
        public InitializedBuilder title(String title)
        {
            this.title = title;
            return this;
        }

        @Override
        public AccessDeniedTemplateTypeBuilder urlTemplate(String urlTemplate)
        {
            this.urlTemplate = urlTemplate;
            return this;
        }

        @Override
        public InitializedBuilder accessDeniedTemplateType(RedirectData.AccessDeniedTemplateType accessDeniedTemplateType)
        {
            this.accessDeniedTemplateType = accessDeniedTemplateType;
            return this;
        }

        @Override
        public RedirectData build()
        {
            Condition condition = connectConditionFactory.createCondition(addOnKey, conditionalBeans, Collections.emptyList());
            return new RedirectData(title, urlTemplate, condition, accessDeniedTemplateType);
        }
    }
}
