package com.atlassian.plugin.connect.spi.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;

import javax.inject.Inject;
import javax.inject.Named;
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
public class PageConditionsValidator
{
    private final PageConditionsFactory pageConditionsFactory;

    @Inject
    public PageConditionsValidator(PageConditionsFactory pageConditionsFactory)
    {
        this.pageConditionsFactory = pageConditionsFactory;
    }

    public void validate(ShallowConnectAddonBean addonBean, List<ConnectPageModuleBean> pages, String descriptorKey) throws ConnectModuleValidationException
    {
        List<SingleConditionBean> conditions = getPageModuleConditions(pages);
        
        for(SingleConditionBean condition : conditions)
        {
            String conditionString = condition.getCondition();
            if (!pageConditionsFactory.getConditionNames().contains(conditionString) && !isRemoteCondition(conditionString))
            {
                String exceptionMessage = String.format("The add-on (%s) includes a Page Module with an unsupported condition (%s)", addonBean.getKey(),conditionString);

                throw new ConnectModuleValidationException(descriptorKey, exceptionMessage);
            }
        }
    }

    private List<SingleConditionBean> getPageModuleConditions(List<ConnectPageModuleBean> pages)
    {
        List<SingleConditionBean> singleConditions = new ArrayList<SingleConditionBean>();

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
}
