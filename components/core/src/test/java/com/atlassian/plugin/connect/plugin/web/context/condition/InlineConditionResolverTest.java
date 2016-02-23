package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.plugin.web.condition.PluggableConditionClassAccessor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.web.Condition;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InlineConditionResolverTest {
    private final PluggableConditionClassAccessor conditionClassAccessor = mock(PluggableConditionClassAccessor.class);
    private final HostContainer hostContainer = mock(HostContainer.class);
    private final InlineConditionResolver resolver = new InlineConditionResolver(conditionClassAccessor, hostContainer);

    @Test
    public void emptyIsReturnedIfConditionThrowsAnExceptionInInitMethod() {
        when(conditionClassAccessor.getConditionClassForInline(any(SingleConditionBean.class))).thenReturn(Optional.of(Condition.class));
        when(hostContainer.create(Condition.class)).thenReturn(new Condition() {
            @Override
            public void init(final Map<String, String> params) throws PluginParseException {
                throw new PluginParseException("sorry, mate");
            }

            @Override
            public boolean shouldDisplay(final Map<String, Object> context) {
                return true;
            }
        });

        Optional<Boolean> result = resolver.resolve(new InlineCondition("condition", Collections.emptyMap()), Collections.emptyMap());
        assertThat(result, equalTo(Optional.empty()));
    }

    @Test
    public void emptyIsReturnedIfConditionThrowsAnExceptionInShouldDisplayMethod() {
        when(conditionClassAccessor.getConditionClassForInline(any(SingleConditionBean.class))).thenReturn(Optional.of(Condition.class));
        when(hostContainer.create(Condition.class)).thenReturn(new Condition() {
            @Override
            public void init(final Map<String, String> params) throws PluginParseException {
            }

            @Override
            public boolean shouldDisplay(final Map<String, Object> context) {
                throw new NullPointerException();
            }
        });

        Optional<Boolean> result = resolver.resolve(new InlineCondition("condition", Collections.emptyMap()), Collections.emptyMap());
        assertThat(result, equalTo(Optional.empty()));
    }

    @Test
    public void emptyIsReturnedIfConditionCannotBeCreatedFromClass() {
        when(conditionClassAccessor.getConditionClassForInline(any(SingleConditionBean.class))).thenReturn(Optional.of(Condition.class));
        when(hostContainer.create(Condition.class)).thenThrow(RuntimeException.class);

        Optional<Boolean> result = resolver.resolve(new InlineCondition("condition", Collections.emptyMap()), Collections.emptyMap());
        assertThat(result, equalTo(Optional.empty()));
    }
}
