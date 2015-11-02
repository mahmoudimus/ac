package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.web.condition.PageConditionsFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.plugin.web.condition.PageConditionsFactoryImpl.USER_IS_ADMIN;
import static com.atlassian.plugin.connect.plugin.web.condition.PageConditionsFactoryImpl.USER_IS_LOGGED_IN;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WebItemModuleProviderImplTest
{

    private WebItemModuleProviderImpl provider;

    @Before
    public void setUp()
    {
        provider = new WebItemModuleProviderImpl(null, null, null, null, null, new PageConditionsFactoryImpl());
    }

    @Test
    public void shouldRetainEmptyConditionsForIframe()
    {
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(Collections.<ConditionalBean>emptyList())), empty());
    }

    @Test
    public void shouldRetainConditionsForIframe()
    {
        List<ConditionalBean> conditions = newArrayList(newCondition(USER_IS_LOGGED_IN),
                newCompositeConditionBean().withConditions(newCondition(USER_IS_ADMIN)).build());
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)), contains(conditions.toArray()));
    }

    @Test
    public void shouldExcludeTopLevelInvalidConditionForIframe()
    {
        CompositeConditionBean compositeCondition = newCompositeConditionBean().withConditions(newCondition(USER_IS_ADMIN)).build();
        List<ConditionalBean> conditions = newArrayList(newCondition("foo"), compositeCondition);
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)), contains(compositeCondition));
    }

    @Test
    public void shouldExcludeSingleNestedInvalidConditionForIframe()
    {
        SingleConditionBean singleCondition = newCondition(USER_IS_LOGGED_IN);
        List<ConditionalBean> conditions = newArrayList(singleCondition,
                newCompositeConditionBean().withConditions(newCondition("foo")).build());
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)), contains(singleCondition));
    }

    @Test
    public void shouldExcludeNestedInvalidConditionForIframeAndLeaveValid()
    {
        SingleConditionBean singleCondition = newCondition(USER_IS_LOGGED_IN);
        List<ConditionalBean> conditions = newArrayList(singleCondition,
                newCompositeConditionBean().withConditions(newCondition("foo"), newCondition(USER_IS_ADMIN)).build());
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)),
                contains(singleCondition, newCompositeConditionBean().withConditions(newCondition(USER_IS_ADMIN)).build()));
    }

    private WebItemModuleBean newWebItemWithConditions(Collection<ConditionalBean> conditions)
    {
        return newWebItemBean().withConditions(conditions).build();
    }

    private SingleConditionBean newCondition(String condition)
    {
        return newSingleConditionBean().withCondition(condition).build();
    }
}
