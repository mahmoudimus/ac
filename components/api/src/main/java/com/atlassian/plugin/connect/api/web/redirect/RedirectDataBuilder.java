package com.atlassian.plugin.connect.api.web.redirect;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;

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

