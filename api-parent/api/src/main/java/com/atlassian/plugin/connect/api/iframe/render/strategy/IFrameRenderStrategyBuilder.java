package com.atlassian.plugin.connect.api.iframe.render.strategy;

import com.atlassian.plugin.connect.api.capabilities.provider.ModuleTemplate;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.web.Condition;

public interface IFrameRenderStrategyBuilder
{
    AddOnUriBuilder addOn(String key);

    interface AddOnUriBuilder
    {
        ModuleUriBuilder module(String key);
    }

    interface ModuleUriBuilder
    {
        TemplatedBuilder pageTemplate();
        TemplatedBuilder genericBodyTemplate();
        TemplatedBuilder genericBodyTemplate(boolean inline);
        TemplatedBuilder dialogTemplate();
        TemplatedBuilder template(ModuleTemplate template);
    }

    interface TemplatedBuilder
    {
        InitializedBuilder urlTemplate(String urlTemplate);
    }

    interface InitializedBuilder
    {
        InitializedBuilder condition(ConditionalBean condition);
        InitializedBuilder conditions(Iterable<ConditionalBean> conditions);
        InitializedBuilder conditionClass(Class<? extends Condition> condition);
        InitializedBuilder conditionClasses(Iterable<Class<? extends Condition>> conditions);
        InitializedBuilder title(String title);
        InitializedBuilder dimensions(String width, String height);
        InitializedBuilder decorator(String decorator);
        InitializedBuilder additionalRenderContext(String key, Object object);
        InitializedBuilder ensureUniqueNamespace(boolean uniqueNamespace);
        InitializedBuilder dialog(boolean isDialog);
        InitializedBuilder simpleDialog(boolean isSimpleDialog);
        InitializedBuilder resizeToParent(boolean resizeToParent);
        InitializedBuilder sign(boolean sign);

        IFrameRenderStrategy build();
    }
}
