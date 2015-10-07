package com.atlassian.plugin.connect.confluence;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.sal.api.component.ComponentLocator;

import java.util.List;
import java.util.Map;

@ConfluenceComponent
@ExportAsDevService
public class ConfluenceWebFragmentHelper implements WebFragmentHelper
{

    private WebFragmentHelper delegate;

    public ConfluenceWebFragmentHelper()
    {
        this.delegate = ComponentLocator.getComponent(WebFragmentHelper.class);
    }

    @Override
    public Condition loadCondition(String s, Plugin plugin) throws ConditionLoadingException
    {
        return delegate.loadCondition(s, plugin);
    }

    @Override
    public ContextProvider loadContextProvider(String s, Plugin plugin) throws ConditionLoadingException
    {
        return delegate.loadContextProvider(s, plugin);
    }

    @Override
    public String getI18nValue(String s, List<?> list, Map<String, Object> map)
    {
        return delegate.getI18nValue(s, list, map);
    }

    @Override
    public String renderVelocityFragment(String s, Map<String, Object> map)
    {
        return delegate.renderVelocityFragment(s, map);
    }
}
