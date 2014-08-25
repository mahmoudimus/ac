package com.atlassian.plugin.connect.plugin.capabilities.validate.impl;

import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.sal.api.message.I18nResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ConditionUtils.isRemoteCondition;

/**
 * If the add-on has ConnectPageModuleBean with conditions, this validator checks that conditions are either remote conditions
 * or are in the list of allowed conditions for pages
 *
 * @since 1.0
 */
@Named("page-conditions-validator")
public class PageConditionsValidator implements AddOnBeanValidator
{
    private static final Logger log = LoggerFactory.getLogger(PageConditionsValidator.class);
    private final I18nResolver i18nResolver;

    @Inject
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
                String exceptionMessage = String.format("The add-on (%s) includes a Page Module with an unsupported condition (%s)", addonBean.getKey(),conditionString);

                String i18nMessage = i18nResolver.getText("connect.install.error.page.with.invalid.condition", addonBean.getKey(),conditionString);
                
                throw new InvalidDescriptorException(exceptionMessage, i18nMessage);
            }
        }
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

        Field[] fields = JiraConfluenceModuleList.class.getDeclaredFields();
        
        for(Field field : fields)
        {
            try
            {
                if(ConnectPageModuleBean.class.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    ConnectPageModuleBean pageModule = (ConnectPageModuleBean) field.get(addonBean.getModules());
                    
                    if(null != pageModule)
                    {
                        pages.add(pageModule);
                    }
                }
                else if(ConnectReflectionHelper.isParameterizedListWithType(field.getGenericType(),ConnectPageModuleBean.class))
                {
                    field.setAccessible(true);

                    List<ConnectPageModuleBean> pageModuleList = (List<ConnectPageModuleBean>) field.get(addonBean.getModules());

                    if(null != pageModuleList)
                    {
                        pages.addAll(pageModuleList);
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Error reflectively looking up page modules for validation", e);
            }
        }
       
        return pages;
    }
}
