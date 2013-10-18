package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ParamBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ParamsBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean;

/**
 * @since 1.0
 */
public class SingleConditionBeanBuilder extends BaseCapabilityBeanBuilder<SingleConditionBeanBuilder,SingleConditionBean>
{
    private String condition;
    private Boolean invert;
    private Map<String,String> params;
    
    public SingleConditionBeanBuilder()
    {
    }

    public SingleConditionBeanBuilder(SingleConditionBean defaultBean)
    {
        this.condition = defaultBean.getCondition();
        this.invert = defaultBean.isInvert();
        this.params = defaultBean.getParams();
    }

    public SingleConditionBeanBuilder withCondition(String condition)
    {
        this.condition = condition;
        return this;
    }

    public SingleConditionBeanBuilder withInvert(Boolean invert)
    {
        this.invert = invert;
        return this;
    }

    public SingleConditionBeanBuilder withParams(Map<String,String> params)
    {
        this.params = params;
        return this;
    }
    
    @Override
    public SingleConditionBean build()
    {
        return new SingleConditionBean(this);
    }
}
