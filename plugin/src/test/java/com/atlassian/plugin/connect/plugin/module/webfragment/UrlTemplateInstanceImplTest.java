package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UrlTemplateInstanceImplTest
{
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Mock
    private ContextMapURLSerializer contextMapURLSerializer;

    @Test
    public void shouldSubstituteVariables()
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        final String path = "/foo/bar?arg1=10&arg2=deadEndSt";
        Map<String, Object> context = ImmutableMap.of();

        when(urlVariableSubstitutor.replace(pathTemplate, context)).thenReturn(path);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(context, "fred")).thenReturn(context);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, context,
                "fred");

        assertThat(urlTemplateInstance.getUrlTemplate(), is(equalTo(pathTemplate)));
        assertThat(urlTemplateInstance.getUrlString(), is(equalTo(path)));

        verify(urlVariableSubstitutor, times(1)).replace(pathTemplate, context);
        verify(contextMapURLSerializer, times(1)).getAuthenticatedAddonParameters(context, "fred");
    }

    @Test
    public void shouldThrowBlahXXXXXXXWhenUserDoesNotHavePermissionOnResource()
    {
        fail("Not implemented yet");
    }

    @Test
    public void shouldIdentifyTemplateVariables()
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        Map<String, Object> context = ImmutableMap.of();

        final Set<String> templateVariables = ImmutableSet.of("blah.id", "user.address.street");

        when(urlVariableSubstitutor.getContextVariables(pathTemplate)).thenReturn(templateVariables);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, context,
                "fred");

        assertThat(urlTemplateInstance.getTemplateVariables(), is(equalTo(templateVariables)));

        verify(urlVariableSubstitutor, times(1)).getContextVariables(pathTemplate);

    }

    @Test
    public void shouldIdentifyNotTemplateContextVariables()
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        final String[] anotherParamValue = {"88"};

        Map<String, Object> context = ImmutableMap.<String, Object>builder()
                .put("blah.id", new String[]{"10"})
                .put("user.address.street", new String[]{"deadEndSt"})
                .put("notInUrlTemplate", anotherParamValue)
                .build();

        final Set<String> templateVariables = ImmutableSet.of("blah.id", "user.address.street");
        final Map<String, String[]> nonTemplateVariables = ImmutableMap.of("notInUrlTemplate", anotherParamValue);

        when(urlVariableSubstitutor.getContextVariables(pathTemplate)).thenReturn(templateVariables);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(context, "fred")).thenReturn(context);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, context,
                "fred");

        assertThat(urlTemplateInstance.getNonTemplateContextParameters(), is(equalTo(nonTemplateVariables)));

        verify(urlVariableSubstitutor, times(1)).getContextVariables(pathTemplate);

    }

    @Test
    public void shouldHandleEmptyContexts()
    {
        fail("TODO");
    }

    @Test
    public void shouldHandleZeroPathVariables()
    {
        fail("TODO");
    }
}
