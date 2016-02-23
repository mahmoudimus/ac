package com.atlassian.plugin.connect.plugin.web.item;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationBlacklist;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
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
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType.OR;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
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
        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(Collections.<ConditionalBean>emptyList()).getConditions()), empty());
    }

    @Test
    public void shouldRetainConditionsForIframe()
    {
        List<ConditionalBean> conditions = newArrayList(newCondition(VALID_CONDITION),
                newCompositeConditionBean().withConditions(newCondition(OTHER_VALID_CONDITION)).build());
        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(conditions).getConditions()), contains(conditions.toArray()));
    }

    @Test
    public void shouldExcludeTopLevelInvalidConditionForIframe()
    {
        CompositeConditionBean compositeCondition = newCompositeConditionBean().withConditions(newCondition(OTHER_VALID_CONDITION)).build();
        List<ConditionalBean> conditions = newArrayList(newCondition("foo"), compositeCondition);
        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(conditions).getConditions()), contains(compositeCondition));
    }

    @Test
    public void shouldExcludeSingleNestedInvalidConditionForIframe()
    {
        SingleConditionBean singleCondition = newCondition(VALID_CONDITION);
        List<ConditionalBean> conditions = newArrayList(singleCondition,
                newCompositeConditionBean().withConditions(newCondition("foo")).build());
        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(conditions).getConditions()), contains(singleCondition));
    }

    @Test
    public void shouldExcludeNestedInvalidAndConditionForIframeAndLeaveValid()
    {
        SingleConditionBean singleCondition = newCondition(VALID_CONDITION);

        List<ConditionalBean> conditions = newArrayList(
            singleCondition,
            newCompositeConditionBean()
                .withConditions(newCondition("foo"), newCondition(OTHER_VALID_CONDITION))
                .withType(CompositeConditionType.AND)
                .build()
        );

        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(conditions).getConditions()),
                contains(singleCondition, newCompositeConditionBean().withConditions(newCondition(OTHER_VALID_CONDITION)).build()));
    }

    @Test
    public void shouldRemoveAllOrNestedInvalidOrConditionsForIframeAndLeaveValid()
    {
        SingleConditionBean singleCondition = newCondition(VALID_CONDITION);

        List<ConditionalBean> conditions = newArrayList(
            singleCondition,
            newCompositeConditionBean()
                .withConditions(newCondition("foo"), newCondition(OTHER_VALID_CONDITION))
                .withType(OR)
                .build()
        );

        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(conditions).getConditions()), contains(singleCondition));

        assertThat(provider.filterProductSpecificConditions(newWebItemWithConditions(conditions).getConditions()),
            not(hasComplexConditionalBeanOfType(OR))
        );
    }

    private static Matcher<List<ConditionalBean>> hasComplexConditionalBeanOfType(CompositeConditionType type) {
        return new BaseMatcher<List<ConditionalBean>>()
        {
            @Override
            public boolean matches(Object o)
            {
                if (o instanceof List)
                {
                    List<ConditionalBean> conditionalBeans = (List<ConditionalBean>) o;

                    for (ConditionalBean conditionalBean : conditionalBeans)
                    {
                        if (conditionalBean instanceof CompositeConditionBean)
                        {
                            CompositeConditionBean compositeConditionBean = (CompositeConditionBean) conditionalBean;
                            if(type.equals(compositeConditionBean.getType())) {
                                return true;
                            }
                        }
                    }

                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Did not find composite conditional bean of type" + type);
            }
        };
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
