package it.jira.customfield;

import com.atlassian.jira.testkit.beans.CustomFieldResponse;
import com.google.common.base.Objects;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CustomFieldMatchers
{
    private CustomFieldMatchers()
    {
    }

    public static Matcher<CustomFieldResponse> customFieldResponse(
            final String name,
            final String description,
            final String type,
            final String searcher)
    {
        return new TypeSafeMatcher<CustomFieldResponse>()
        {
            @Override
            protected boolean matchesSafely(final CustomFieldResponse customFieldItem)
            {
                return Objects.equal(customFieldItem.name, name)
                        && Objects.equal(customFieldItem.description, description)
                        && Objects.equal(customFieldItem.type, type)
                        && Objects.equal(customFieldItem.searcher, searcher);
            }

            @Override
            public void describeTo(final Description desc)
            {
                desc.appendValue(name)
                        .appendValue(description)
                        .appendValue(type)
                        .appendValue(searcher);
            }
        };
    }
}
