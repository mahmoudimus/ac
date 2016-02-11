package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserIsInGroupConditionTest
{
    @Mock
    private UserProfile userProfile;

    @Mock
    private UserManager userManager;

    @InjectMocks
    private UserIsInGroupCondition sut;

    @Test(expected = PluginParseException.class)
    public void shouldDisplay__missing_group_name_should_throw_exception() {
        sut.init(ImmutableMap.of());
    }

    @Test
    public void shouldDisplay__return_false_for_anonymous_user() {
        when(userManager.getRemoteUser()).thenReturn(null);
        sut.init(ImmutableMap.of("groupName", "test-group"));
        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void shouldDisplay__return_true_if_user_in_group() {
        final UserKey testUserKey = new UserKey("test-user");
        when(userProfile.getUserKey()).thenReturn(testUserKey);
        when(userManager.getRemoteUser()).thenReturn(userProfile);
        when(userManager.isUserInGroup(testUserKey, "test-group")).thenReturn(Boolean.TRUE);
        sut.init(ImmutableMap.of("groupName", "test-group"));
        assertTrue(sut.shouldDisplay(ImmutableMap.of()));
    }

    @Test
    public void shouldDisplay__return_false_if_user_not_in_group() {
        final UserKey testUserKey = new UserKey("test-user");
        when(userProfile.getUserKey()).thenReturn(testUserKey);
        when(userManager.getRemoteUser()).thenReturn(userProfile);
        when(userManager.isUserInGroup(testUserKey, "test-group")).thenReturn(Boolean.FALSE);
        sut.init(ImmutableMap.of("groupName", "test-group"));
        assertFalse(sut.shouldDisplay(ImmutableMap.of()));
    }
}