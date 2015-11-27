package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebItemModuleProviderImplTest
{

    private static final String BLACKLISTED_LOCATION = "blacklistedLocation";
    private static final String VALID_CONDITION = "some-condition";
    private static final String OTHER_VALID_CONDITION = "some-other-condition";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private WebItemModuleProviderImpl provider;

    @Mock
    private ConditionClassAccessor conditionClassAccessor;

    @Mock
    private WebFragmentLocationBlacklist blacklist;

    @Before
    public void setUp()
    {
        when(blacklist.getBlacklistedWebItemLocations()).thenReturn(Sets.newHashSet());
        when(conditionClassAccessor.getConditionClassForNoContext(any(SingleConditionBean.class)))
                .thenReturn(Optional.empty());
        when(conditionClassAccessor.getConditionClassForNoContext(argThat(isSingleConditionBeanFor(VALID_CONDITION))))
                .thenReturn(Optional.of(Condition.class));
        when(conditionClassAccessor.getConditionClassForNoContext(argThat(isSingleConditionBeanFor(OTHER_VALID_CONDITION))))
                .thenReturn(Optional.of(Condition.class));
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

    @Test
    public void shouldThrowExceptionIfBlacklistedLocationIsUsed() throws ConnectModuleValidationException
    {
        ShallowConnectAddonBean descriptor = mock(ShallowConnectAddonBean.class);
        when(blacklist.getBlacklistedWebItemLocations()).thenReturn(Sets.newHashSet(BLACKLISTED_LOCATION));
        List<WebItemModuleBean> webItemModuleBeans = Lists.newArrayList(
                newWebItemBean()
                    .withLocation(BLACKLISTED_LOCATION)
                    .build()
        );
        expectedException.expect(ConnectModuleValidationException.class);
        expectedException.expectMessage(format("Installation failed. The add-on includes a web fragment with an unsupported location ([%s]).", BLACKLISTED_LOCATION));
        provider.assertLocationNotBlacklisted(descriptor, webItemModuleBeans);
    }

    private WebItemModuleBean newWebItemWithConditions(Collection<ConditionalBean> conditions)
    {
        return newWebItemBean().withConditions(conditions).build();
    }

    private SingleConditionBean newCondition(String condition)
    {
        return newSingleConditionBean().withCondition(condition).build();
    }

    private Matcher<SingleConditionBean> isSingleConditionBeanFor(String condition)
    {
        return new TypeSafeMatcher<SingleConditionBean>()
        {

            @Override
            protected boolean matchesSafely(SingleConditionBean conditionBean)
            {
                return condition.equals(conditionBean.getCondition());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Single condition with condition ")
                        .appendValue(condition);
            }
        };
    }
}
