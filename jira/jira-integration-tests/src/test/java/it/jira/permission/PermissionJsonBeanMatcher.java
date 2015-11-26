package it.jira.permission;

import it.jira.permission.PermissionJsonBean.PermissionType;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

class PermissionJsonBeanMatcher extends TypeSafeMatcher<PermissionJsonBean>
{
    private final String key;
    private final PermissionType type;
    private final String name;
    private final String description;

    PermissionJsonBeanMatcher(String key, PermissionType type, String name, String description)
    {
        this.key = key;
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public static PermissionJsonBeanMatcher isPermission(String key, PermissionType type, String name, String description)
    {
        return new PermissionJsonBeanMatcher(key, type, name, description);
    }

    @Override
    protected boolean matchesSafely(PermissionJsonBean item)
    {
        return allOf(
                hasProperty("key", equalTo(key)),
                hasProperty("name", equalTo(name)),
                hasProperty("type", equalTo(type)),
                hasProperty("description", equalTo(description))
        ).matches(item);
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(String.format("(key=%s, name=%s, type=%s, description=%s)", key, name, type, this.description));
    }
}