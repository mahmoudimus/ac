package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.PageConditions;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.sal.api.message.I18nResolver;

import com.google.common.collect.ImmutableList;
import com.nimbusds.jwt.JWT;

/**
 * If the add-on has ConnectPageModuleBean with conditions, this validator checks that conditions are either remote conditions
 * or are in the list of allowed conditions for pages
 *
 * @since 1.0
 */
@Named("page-conditions-validator")
public class PageConditionsValidator implements AddOnBeanValidator
{
    private final I18nResolver i18nResolver;

    public PageConditionsValidator(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    @Override
    public void validate(ConnectAddonBean addonBean) throws InvalidDescriptorException
    {
        List<SingleConditionBean> conditions = getPageModuleConditions(addonBean);
        
        for(SingleConditionBean condition : conditions)
        {
            String conditionString = condition.getCondition();
            if (!PageConditions.CONDITION_SET.contains(conditionString) && !isRemoteCondition(conditionString))
            {
                String exceptionMessage = String.format("The add-on (%s) inludes a Page Module with an unsupported condition (%s)", addonBean.getKey(),conditionString);

                String i18nMessage = i18nResolver.getText("connect.install.error.page.with.invalid.condition", addonBean.getKey(),conditionString);
                
                throw new InvalidDescriptorException(exceptionMessage, i18nMessage);
            }
        }
    }

    private boolean isRemoteCondition(String conditionString)
    {
        return (conditionString.startsWith("http") || conditionString.startsWith("/"));
    }

    private List<SingleConditionBean> getPageModuleConditions(ConnectAddonBean addonBean)
    {
        List<SingleConditionBean> singleConditions = new ArrayList<SingleConditionBean>();
        
        List<ConnectPageModuleBean> pages = getPageModules(addonBean);
        
        for(ConnectPageModuleBean page : pages)
        {
            singleConditions.addAll(extractSingleConditions(page.getConditions()));
        }
        
        return singleConditions;
    }

    private Collection<? extends SingleConditionBean> extractSingleConditions(List<ConditionalBean> conditions)
    {
        List<SingleConditionBean> singleConditions = new ArrayList<SingleConditionBean>();
        
        for(ConditionalBean condition : conditions)
        {
            if(SingleConditionBean.class.isAssignableFrom(condition.getClass()))
            {
                singleConditions.add((SingleConditionBean) condition);
            }
            else
            {
                singleConditions.addAll(extractSingleConditions(((CompositeConditionBean)condition).getConditions()));
            }
        }
        
        return singleConditions;
    }

    private List<ConnectPageModuleBean> getPageModules(ConnectAddonBean addonBean)
    {
        List<ConnectPageModuleBean> pages = new ArrayList<ConnectPageModuleBean>();
        pages.addAll(addonBean.getModules().getAdminPages());
        pages.addAll(addonBean.getModules().getGeneralPages());
        
        if(null != addonBean.getModules().getConfigurePage())
        {
            pages.addAll(ImmutableList.of(addonBean.getModules().getConfigurePage()));
        }
        
        return pages;
    }
}
