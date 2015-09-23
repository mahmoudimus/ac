package com.atlassian.plugin.connect.plugin;

import java.util.Map;

import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class SubjectJwtClaimWriterTest
{
    @Mock
    private JwtJsonBuilder builder;
    @Mock
    private UserManager userManager;
    @Mock
    private UserProfile userProfile;
    @InjectMocks
    private SubjectJwtClaimWriter claimWriter;

    @SuppressWarnings ("unchecked")
    @Test
    public void noSubjectAddedWhenNotAutenticated()
    {
        claimWriter.write(builder);

        verify(builder, never()).subject(anyString());

        ArgumentCaptor<Object> customClaimCaptor = ArgumentCaptor.forClass(Object.class);
        verify(builder).claim(eq("context"), customClaimCaptor.capture());
        Map<Object, Object> context = (Map<Object, Object>) customClaimCaptor.getValue();
        assertThat(context.isEmpty(), is(true));
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void subjectAndContextUserAddedWhenAuthenticated()
    {
        when(userManager.getRemoteUser()).thenReturn(userProfile);
        when(userProfile.getUsername()).thenReturn("bwayne");
        when(userProfile.getUserKey()).thenReturn(new UserKey("batman"));
        when(userProfile.getFullName()).thenReturn("Bruce Wayne");

        claimWriter.write(builder);

        ArgumentCaptor<Object> customClaimCaptor = ArgumentCaptor.forClass(Object.class);
        verify(builder).subject(eq("batman"));
        verify(builder).claim(eq("context"), customClaimCaptor.capture());

        Map<String, Object> context = (Map<String, Object>) customClaimCaptor.getValue();
        assertThat(context.size(), is(1));
        assertThat(context.containsKey("user"), is(true));

        Map<String, String> contextUser = (Map<String, String>) context.get("user");
        assertThat(contextUser.size(), is(3));
        assertThat(contextUser.get("username"), is("bwayne"));
        assertThat(contextUser.get("userKey"), is("batman"));
        assertThat(contextUser.get("displayName"), is("Bruce Wayne"));
    }
}