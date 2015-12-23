package com.atlassian.plugin.connect.jira.web.dashboard;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@JiraComponent
public class ConnectDashboardItemContextParameterMapper
{

    private static final String DASHBOARD_CONTEXT_KEY = "dashboard";
    private static final String DASHBOARD_ID_FIELD_KEY = "id";

    private static final String ITEM_CONTEXT_KEY = "dashboardItem";
    private static final String ITEM_KEY_FIELD_KEY = "moduleKey";
    private static final String ITEM_ID_FIELD_KEY = "id";

    private static final String VIEW_CONTEXT_KEY = "view";
    private static final String VIEW_TYPE_FIELD_KEY = "viewType";
    private static final String VIEW_TYPE_NAME_FIELD_KEY = "viewType";

    private static final String DASHBOARD_ID_PARAMETER_KEY = "dashboard.id";
    private static final String ITEM_KEY_PARAMETER_KEY = "dashboardItem.key";
    private static final String ITEM_ID_PARAMETER_KEY = "dashboardItem.id";
    private static final String VIEW_TYPE_PARAMETER_KEY = "dashboardItem.viewType";

    public Map<String, String> extractContextParameters(Map<String, Object> context)
    {
        Map<String, String> contextParameters = new HashMap<>();
        Optional<Optional<String>> optionalViewType = getMapEntryOfType(context, VIEW_CONTEXT_KEY, Map.class)
                .map((contextValue) -> getNestedMapEntryOfType(contextValue, VIEW_TYPE_FIELD_KEY, VIEW_TYPE_NAME_FIELD_KEY, String.class));
        putContextParameter(contextParameters, VIEW_TYPE_PARAMETER_KEY, optionalViewType.orElseGet(Optional::empty));
        putContextParameter(contextParameters, ITEM_KEY_PARAMETER_KEY,
                getNestedMapEntryOfType(context, ITEM_CONTEXT_KEY, ITEM_KEY_FIELD_KEY, String.class));
        putContextParameter(contextParameters, DASHBOARD_ID_PARAMETER_KEY,
                getNestedMapEntryOfType(context, DASHBOARD_CONTEXT_KEY, DASHBOARD_ID_FIELD_KEY, String.class));
        putContextParameter(contextParameters, ITEM_ID_PARAMETER_KEY,
                getNestedMapEntryOfType(context, ITEM_CONTEXT_KEY, ITEM_ID_FIELD_KEY, String.class));
        return contextParameters;
    }

    private void putContextParameter(Map<String, String> contextParameters, String parameterKey, Optional<String> optionalParameterValue)
    {
        optionalParameterValue.ifPresent((itemKey) -> contextParameters.put(parameterKey, itemKey));
    }

    private static <T> Optional<T> getNestedMapEntryOfType(Map<String, Object> context, String key, String nestedKey, Class<T> nestedValueClass)
    {
        Optional<Optional<T>> optioanlContextValue = getMapEntryOfType(context, key, Map.class)
                .map((contextValue) -> getMapEntryOfType(contextValue, nestedKey, nestedValueClass));
        return optioanlContextValue.orElseGet(Optional::empty);
    }

    private static <T> Optional<T> getMapEntryOfType(Map<String, Object> context, String key, Class<T> valueClass)
    {
        return Optional.ofNullable(context.get(key))
                .filter(valueClass::isInstance)
                .map(valueClass::cast);
    }
}
