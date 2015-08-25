package com.atlassian.plugin.connect.plugin.redirect;

import com.atlassian.plugin.connect.api.capabilities.provider.ModuleTemplate;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.condition.ConnectConditionFactory;
import com.atlassian.plugin.connect.plugin.redirect.RedirectData.AccessDeniedTemplateType;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class RedirectDataBuilderBuilderFactoryImpl implements RedirectDataBuilderFactory
{
    private final ConnectConditionFactory connectConditionFactory;

    @Autowired
    public RedirectDataBuilderBuilderFactoryImpl(final ConnectConditionFactory connectConditionFactory)
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
        private AccessDeniedTemplateType accessDeniedTemplateType;

        RedirectDataBuilderImpl(final ConnectConditionFactory connectConditionFactory)
        {
            this.connectConditionFactory = connectConditionFactory;
        }

        @Override
        public ModuleUriBuilder addOn(final String key)
        {
            addOnKey = checkNotNull(key);
            return this;
        }

        @Override
        public InitializedBuilder conditions(final Iterable<ConditionalBean> conditions)
        {
            Iterables.addAll(conditionalBeans, conditions);
            return this;
        }

        @Override
        public InitializedBuilder title(final String title)
        {
            this.title = title;
            return this;
        }

        @Override
        public AccessDeniedTemplateTypeBuilder urlTemplate(final String urlTemplate)
        {
            this.urlTemplate = this.urlTemplate;
            return this;
        }

        @Override
        public InitializedBuilder accessDeniedTemplateType(final AccessDeniedTemplateType accessDeniedTemplateType)
        {
            this.accessDeniedTemplateType = accessDeniedTemplateType;
            return this;
        }

        @Override
        public RedirectData build()
        {
            Condition condition = connectConditionFactory.createCondition(addOnKey, conditionalBeans);
            return new RedirectData(title, urlTemplate, condition, accessDeniedTemplateType);
        }
    }
}
