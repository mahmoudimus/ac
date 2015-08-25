package com.atlassian.plugin.connect.plugin.redirect;

import com.atlassian.plugin.connect.api.capabilities.provider.ModuleTemplate;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.web.Condition;

public interface RedirectDataBuilder
{
    ModuleUriBuilder addOn(String key);

    interface ModuleUriBuilder
    {
        AccessDeniedTemplateTypeBuilder urlTemplate(String template);
    }

    interface AccessDeniedTemplateTypeBuilder
    {
        InitializedBuilder accessDeniedTemplateType(RedirectData.AccessDeniedTemplateType accessDeniedTemplateType);
    }

    interface InitializedBuilder
    {
        InitializedBuilder conditions(Iterable<ConditionalBean> conditions);
        InitializedBuilder title(String title);
        RedirectData build();
    }
}
