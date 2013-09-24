package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
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
    public void shouldCopeWithEmptyParams() throws InvalidContextParameterException
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        Map<String, Object> context = ImmutableMap.of();

        when(urlVariableSubstitutor.replace(pathTemplate, context)).thenReturn(pathTemplate);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, context,
                "fred");

        assertThat(urlTemplateInstance.getUrlTemplate(), is(equalTo(pathTemplate)));
        assertThat(urlTemplateInstance.getUrlString(), is(equalTo(pathTemplate)));

        verify(contextMapURLSerializer, times(1)).getAuthenticatedAddonParameters(context, "fred");
        verify(urlVariableSubstitutor, times(1)).replace(pathTemplate, context);
    }

    @Test
    public void shouldCopeWithZeroPathVariables() throws InvalidContextParameterException
    {
        final String pathTemplate = "/foo/bar";
        Map<String, Object> context = ImmutableMap.<String, Object>of("somekey", new String[]{"somevalue"});

        when(urlVariableSubstitutor.replace(pathTemplate, context)).thenReturn(pathTemplate);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(context, "fred")).thenReturn(context);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, context,
                "fred");

        assertThat(urlTemplateInstance.getUrlTemplate(), is(equalTo(pathTemplate)));
        assertThat(urlTemplateInstance.getUrlString(), is(equalTo(pathTemplate)));

        verify(contextMapURLSerializer, times(1)).getAuthenticatedAddonParameters(context, "fred");
        verify(urlVariableSubstitutor, times(1)).replace(pathTemplate, context);
    }

    @Test
    public void shouldSubstituteVariables() throws InvalidContextParameterException
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        final String path = "/foo/bar?arg1=10&arg2=deadEndSt";
        Map<String, Object> requestParams = ImmutableMap.of();

        when(urlVariableSubstitutor.replace(pathTemplate, requestParams)).thenReturn(path);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(requestParams, "fred")).thenReturn(requestParams);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, requestParams,
                "fred");

        assertThat(urlTemplateInstance.getUrlTemplate(), is(equalTo(pathTemplate)));
        assertThat(urlTemplateInstance.getUrlString(), is(equalTo(path)));

        verify(urlVariableSubstitutor, times(1)).replace(pathTemplate, requestParams);
        verify(contextMapURLSerializer, times(1)).getAuthenticatedAddonParameters(requestParams, "fred");
    }

    @Test
    public void shouldExtractContextAndAddToParamMap() throws InvalidContextParameterException
    {
        String[] contextStr = new String[]{"{\"project\":{\"id\":10100}}"};

        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("context", contextStr, "someparam", "somevalue");
        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams,
                "fred");

        final ArgumentCaptor<Map> paramCaptor = ArgumentCaptor.forClass(Map.class);
        verify(contextMapURLSerializer, times(1)).getAuthenticatedAddonParameters(paramCaptor.capture(), eq("fred"));

        final Map<String, Object> contextMap = paramCaptor.getValue();
        assertThat(contextMap, hasKey("project"));
        final Map<String, Object> project = (Map<String, Object>) contextMap.get("project");
        assertThat(project, hasEntry("id", (Object) 10100));
        assertThat(contextMap, hasEntry("someparam", (Object) "somevalue"));
        assertThat(contextMap, not(hasKey("context")));
    }


    @Test
    public void shouldThrowBlahXXXXXXXWhenUserDoesNotHavePermissionOnResource()
    {
        fail("Not implemented yet");
    }

    @Test(expected = InvalidContextParameterException.class)
    public void shouldThrowInvalidContextParameterExceptionWhenCantUnmarshalJsonContext() throws InvalidContextParameterException
    {
        String[] contextStr = new String[]{"not proper json"};

        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("context", contextStr);
        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");
    }

    @Test(expected = InvalidContextParameterException.class)
    public void shouldThrowInvalidContextParameterExceptionWhenContextJsonEmptyStringArray() throws InvalidContextParameterException
    {
        String[] contextStr = new String[]{};

        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("context", contextStr);
        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");

    }

    @Test
    public void shouldIdentifyTemplateVariables() throws InvalidContextParameterException
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        Map<String, Object> requestParams = ImmutableMap.of();

        final Set<String> templateVariables = ImmutableSet.of("blah.id", "user.address.street");

        when(urlVariableSubstitutor.getContextVariables(pathTemplate)).thenReturn(templateVariables);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor,
                contextMapURLSerializer, pathTemplate, requestParams, "fred");

        assertThat(urlTemplateInstance.getTemplateVariables(), is(equalTo(templateVariables)));

        verify(urlVariableSubstitutor, times(1)).getContextVariables(pathTemplate);

    }

}
