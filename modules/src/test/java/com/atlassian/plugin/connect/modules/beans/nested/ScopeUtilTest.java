package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.ADMIN;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.DELETE;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.READ;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.SPACE_ADMIN;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeName.WRITE;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil.isTransitionDownToReadOrLess;
import static com.atlassian.plugin.connect.modules.beans.nested.ScopeUtil.isTransitionUpFromReadOrLess;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ScopeUtilTest
{

    private static Set<ScopeName> EMPTY = ImmutableSet.of();

    @Test
    public void notTransitionDownWhenBothSetsEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(EMPTY, EMPTY), is(false));
    }

    @Test
    public void notTransitionDownWhenPreviousEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(EMPTY, ImmutableSet.of(READ)), is(false));
    }

    @Test
    public void notTransitionDownWhenPreviousReadAndNewEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(READ), EMPTY), is(false));
    }

    @Test
    public void notTransitionDownWhenPreviousReadAndNewRead()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(READ), ImmutableSet.of(READ)), is(false));
    }

    @Test
    public void isTransitionDownWhenPreviousWriteAndNewEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(WRITE), EMPTY), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousDeleteAndNewEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(DELETE), EMPTY), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousSpaceAdminAndNewEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(SPACE_ADMIN), EMPTY), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousAdminAndNewEmpty()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(ADMIN), EMPTY), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousWriteAndNewRead()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(WRITE), ImmutableSet.of(READ)), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousDeleteAndNewRead()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(DELETE), ImmutableSet.of(READ)), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousSpaceAdminAndNewRead()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(SPACE_ADMIN), ImmutableSet.of(READ)), is(true));
    }

    @Test
    public void isTransitionDownWhenPreviousAdminAndNewRead()
    {
        assertThat(isTransitionDownToReadOrLess(ImmutableSet.of(ADMIN), ImmutableSet.of(READ)), is(true));
    }

    // transition up tests

    @Test
    public void notTransitionUpWhenBothSetsEmpty()
    {
        assertThat(isTransitionUpFromReadOrLess(EMPTY, EMPTY), is(false));
    }

    @Test
    public void notTransitionUpWhenNewEmpty()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(READ), EMPTY), is(false));
    }

    @Test
    public void notTransitionUpWhenPreviousWriteAndNewWrite()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(WRITE), ImmutableSet.of(WRITE)), is(false));
    }

    @Test
    public void notTransitionUpWhenPreviousWriteAndNewAdmin()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(WRITE), ImmutableSet.of(ADMIN)), is(false));
    }

    @Test
    public void isTransitionUpWhenPreviousEmptyAndNewWrite()
    {
        assertThat(isTransitionUpFromReadOrLess(EMPTY, ImmutableSet.of(WRITE)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousEmptyAndNewDelete()
    {
        assertThat(isTransitionUpFromReadOrLess(EMPTY, ImmutableSet.of(DELETE)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousEmptyAndNewSpaceAdmin()
    {
        assertThat(isTransitionUpFromReadOrLess(EMPTY, ImmutableSet.of(SPACE_ADMIN)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousEmptyAndNewAdmin()
    {
        assertThat(isTransitionUpFromReadOrLess(EMPTY, ImmutableSet.of(ADMIN)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousReadAndNewWrite()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(READ), ImmutableSet.of(WRITE)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousReadAndNewDelete()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(READ), ImmutableSet.of(DELETE)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousReadAndNewSpaceAdmin()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(READ), ImmutableSet.of(SPACE_ADMIN)), is(true));
    }

    @Test
    public void isTransitionUpWhenPreviousReadAndNewAdmin()
    {
        assertThat(isTransitionUpFromReadOrLess(ImmutableSet.of(READ), ImmutableSet.of(ADMIN)), is(true));
    }
}
