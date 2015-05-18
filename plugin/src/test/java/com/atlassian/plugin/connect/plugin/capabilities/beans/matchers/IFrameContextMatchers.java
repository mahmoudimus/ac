package com.atlassian.plugin.connect.plugin.capabilities.beans.matchers;

import com.atlassian.plugin.connect.spi.module.IFrameContext;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class IFrameContextMatchers
{
    public static ArgumentMatcher<IFrameContext> hasIFramePath(final String url)
    {
        assertThat(url, is(not(nullValue())));

        return new ArgumentMatcher<IFrameContext>()
        {
            @Override
            public boolean matches(Object argument)
            {
                assertThat(argument, is(instanceOf(IFrameContext.class)));
                IFrameContext iFrameContext = (IFrameContext) argument;
                return url.equals(iFrameContext.getIframePath());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("IFrameContext with iFrame URL ");
                description.appendValue(url);
            }
        };
    }

}
