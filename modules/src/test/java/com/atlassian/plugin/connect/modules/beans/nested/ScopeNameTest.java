package com.atlassian.plugin.connect.modules.beans.nested;

import com.google.common.collect.Sets;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

public class ScopeNameTest
{
    @Test
    public void readImpliesNothing()
    {
        assertThat(ScopeName.READ.getImplied(), is(Collections.<ScopeName>emptySet()));
    }

    @Test
    public void writeImpliesRead()
    {
        assertThat(ScopeName.WRITE.getImplied(), is(asSet(ScopeName.READ)));
    }

    @Test
    public void deleteImpliesReadAndWrite()
    {
        assertThat(ScopeName.DELETE.getImplied(), is(asSet(ScopeName.READ, ScopeName.WRITE)));
    }

    @Test
    public void projectAdminImpliesReadAndWriteAndDelete()
    {
        assertThat(ScopeName.PROJECT_ADMIN.getImplied(), is(asSet(ScopeName.READ, ScopeName.WRITE, ScopeName.DELETE)));
    }

    @Test
    public void spaceAdminImpliesReadAndWriteAndDelete()
    {
        assertThat(ScopeName.SPACE_ADMIN.getImplied(), is(asSet(ScopeName.READ, ScopeName.WRITE, ScopeName.DELETE)));
    }

    @Test
    public void adminImpliesReadAndWriteAndDeleteAndProjectAdminAndSpaceAdmin()
    {
        assertThat(ScopeName.ADMIN.getImplied(), is(asSet(ScopeName.READ, ScopeName.WRITE, ScopeName.DELETE, ScopeName.PROJECT_ADMIN, ScopeName.SPACE_ADMIN)));
    }

    private Set<ScopeName> asSet(ScopeName... scopeNames)
    {
        return Sets.newHashSet(scopeNames);
    }

    private Matcher<Collection<ScopeName>> is(Set<ScopeName> scopeNames)
    {
        return Is.is((Collection<ScopeName>) scopeNames);
    }
}
