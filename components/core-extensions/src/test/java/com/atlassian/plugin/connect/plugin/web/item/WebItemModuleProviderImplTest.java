package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebItemModuleProviderImplTest
{

    private static final String VALID_CONDITION = "some-condition";
    private static final String OTHER_VALID_CONDITION = "some-other-condition";

    @InjectMocks
    private WebItemModuleProviderImpl provider;

    @Mock
    private PluginAccessor pluginAccessor;

    @Before
    public void setUp()
    {
        ConnectConditionClassResolver resolver = () -> Lists.newArrayList(
                ConnectConditionClassResolver.Entry.newEntry(VALID_CONDITION, Condition.class).contextFree().build(),
                ConnectConditionClassResolver.Entry.newEntry(OTHER_VALID_CONDITION, Condition.class).contextFree().build()
        );
        when(pluginAccessor.getEnabledModulesByClass(ConnectConditionClassResolver.class)).thenReturn(Collections.singletonList(resolver));
    }

    @Test
    public void shouldRetainEmptyConditionsForIframe()
    {
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(Collections.<ConditionalBean>emptyList())), empty());
    }

    @Test
    public void shouldRetainConditionsForIframe()
    {
        List<ConditionalBean> conditions = newArrayList(newCondition(VALID_CONDITION),
                newCompositeConditionBean().withConditions(newCondition(OTHER_VALID_CONDITION)).build());
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)), contains(conditions.toArray()));
    }

    @Test
    public void shouldExcludeTopLevelInvalidConditionForIframe()
    {
        CompositeConditionBean compositeCondition = newCompositeConditionBean().withConditions(newCondition(OTHER_VALID_CONDITION)).build();
        List<ConditionalBean> conditions = newArrayList(newCondition("foo"), compositeCondition);
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)), contains(compositeCondition));
    }

    @Test
    public void shouldExcludeSingleNestedInvalidConditionForIframe()
    {
        SingleConditionBean singleCondition = newCondition(VALID_CONDITION);
        List<ConditionalBean> conditions = newArrayList(singleCondition,
                newCompositeConditionBean().withConditions(newCondition("foo")).build());
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)), contains(singleCondition));
    }

    @Test
    public void shouldExcludeNestedInvalidConditionForIframeAndLeaveValid()
    {
        SingleConditionBean singleCondition = newCondition(VALID_CONDITION);
        List<ConditionalBean> conditions = newArrayList(singleCondition,
                newCompositeConditionBean().withConditions(newCondition("foo"), newCondition(OTHER_VALID_CONDITION)).build());
        assertThat(provider.getConditionsForIframe(newWebItemWithConditions(conditions)),
                contains(singleCondition, newCompositeConditionBean().withConditions(newCondition(OTHER_VALID_CONDITION)).build()));
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
