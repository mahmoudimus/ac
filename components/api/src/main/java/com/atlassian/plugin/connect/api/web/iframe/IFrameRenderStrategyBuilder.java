package com.atlassian.plugin.connect.api.web.iframe;

import com.atlassian.plugin.connect.api.web.ModuleTemplate;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.web.Condition;

public interface IFrameRenderStrategyBuilder {
    AddonUriBuilder addon(String key);

    interface AddonUriBuilder {
        ModuleUriBuilder module(String key);
    }

    interface ModuleUriBuilder {
        TemplatedBuilder pageTemplate();

        TemplatedBuilder genericBodyTemplate();

        TemplatedBuilder genericBodyTemplate(boolean inline);

        TemplatedBuilder dialogTemplate();

        TemplatedBuilder template(ModuleTemplate template);
    }

    interface TemplatedBuilder {
        InitializedBuilder urlTemplate(String urlTemplate);
    }

    interface InitializedBuilder {
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

        InitializedBuilder redirect(boolean redirect);

        IFrameRenderStrategy build();
    }
}
