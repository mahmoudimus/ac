package com.atlassian.plugin.connect.plugin.redirect;

import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Bunch of data requred to created redirect response by {@link RedirectServlet}.
 */

public final class RedirectData
{
    private static final String TEMPLATE_PATH = "velocity/";
    private static final String TEMPLATE_ACCESS_DENIED_PAGE = TEMPLATE_PATH + "iframe-page-accessdenied.vm";
    private static final String TEMPLATE_ACCESS_DENIED_GENERIC_BODY = TEMPLATE_PATH + "iframe-body-accessdenied.vm";
    private static final Map<AccessDeniedTemplateType, String> accessDeniedTemplateTypeToPath = ImmutableMap.of(
            AccessDeniedTemplateType.PAGE, TEMPLATE_ACCESS_DENIED_PAGE,
            AccessDeniedTemplateType.IFRAME, TEMPLATE_ACCESS_DENIED_GENERIC_BODY
    );

    private final String title;
    private final String urlTemplate;
    private final Condition condition;
    private final AccessDeniedTemplateType accessDeniedTemplateType;

    public RedirectData(String title, String urlTemplate, Condition condition, AccessDeniedTemplateType accessDeniedTemplateType)
    {
        this.title = title;
        this.urlTemplate = urlTemplate;
        this.condition = condition;
        this.accessDeniedTemplateType = accessDeniedTemplateType;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public String getAccessDeniedTemplate()
    {
        return accessDeniedTemplateTypeToPath.get(accessDeniedTemplateType);
    }

    public boolean shouldRedirect(Map<String, ? extends Object> conditionContext)
    {
        return condition == null || condition.shouldDisplay((Map<String, Object>) conditionContext);
    }

    public enum AccessDeniedTemplateType
    {
        PAGE,
        IFRAME
    }
}
