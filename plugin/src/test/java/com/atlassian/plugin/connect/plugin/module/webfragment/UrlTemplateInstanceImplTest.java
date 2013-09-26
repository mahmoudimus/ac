package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UrlTemplateInstanceImplTest
{
    @Mock
    private UrlVariableSubstitutor urlVariableSubstitutor;

    @Mock
    private ContextMapURLSerializer contextMapURLSerializer;

    @Test
    public void shouldCopeWithEmptyParams() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
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
    public void shouldCopeWithZeroPathVariables() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
    {
        final String pathTemplate = "/foo/bar";
        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("somekey", new String[]{"somevalue"});
        Map<String, Object> expectedNestedParams = ImmutableMap.<String, Object>of("somekey", "somevalue");

        when(urlVariableSubstitutor.replace(pathTemplate, requestParams)).thenReturn(pathTemplate);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(expectedNestedParams, "fred")).thenReturn(requestParams);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, requestParams,
                "fred");

        assertThat(urlTemplateInstance.getUrlTemplate(), is(equalTo(pathTemplate)));
        assertThat(urlTemplateInstance.getUrlString(), is(equalTo(pathTemplate)));

        verify(contextMapURLSerializer, times(1)).getAuthenticatedAddonParameters(expectedNestedParams, "fred");
        verify(urlVariableSubstitutor, times(1)).replace(pathTemplate, requestParams);
    }

    @Test
    public void shouldSubstituteVariables() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
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
    @Ignore // Removed support for Json request param form for now
    public void shouldExtractContextAndAddToParamMap() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
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


    @Test(expected = ResourceNotFoundException.class)
    public void shouldLetResourceNotFoundExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException, InvalidContextParameterException
    {
        final Map<String, Object> requestParams = ImmutableMap.<String, Object>of();

        when(contextMapURLSerializer.getAuthenticatedAddonParameters(requestParams, "fred")).thenThrow(new ResourceNotFoundException("blah"));

        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");
    }

    @Test(expected = MalformedRequestException.class)
    public void shouldLetMalformedRequestExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException, InvalidContextParameterException
    {
        final Map<String, Object> requestParams = ImmutableMap.<String, Object>of();

        when(contextMapURLSerializer.getAuthenticatedAddonParameters(requestParams, "fred")).thenThrow(new MalformedRequestException("blah"));

        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");
    }

    @Test(expected = UnauthorisedException.class)
    public void shouldLetUnauthorisedExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException, InvalidContextParameterException
    {
        final Map<String, Object> requestParams = ImmutableMap.<String, Object>of();

        when(contextMapURLSerializer.getAuthenticatedAddonParameters(requestParams, "fred")).thenThrow(new UnauthorisedException("blah"));

        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");
    }

    @Ignore // Removed support for Json request param form for now
    @Test(expected = InvalidContextParameterException.class)
    public void shouldThrowInvalidContextParameterExceptionWhenCantUnmarshalJsonContext() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
    {
        String[] contextStr = new String[]{"not proper json"};

        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("context", contextStr);
        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");
    }

    @Ignore // Removed support for Json request param form for now
    @Test(expected = InvalidContextParameterException.class)
    public void shouldThrowInvalidContextParameterExceptionWhenContextJsonEmptyStringArray() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
    {
        String[] contextStr = new String[]{};

        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("context", contextStr);
        new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, "somePath", requestParams, "fred");

    }

    @Test
    public void shouldIdentifyTemplateVariables() throws InvalidContextParameterException, ResourceNotFoundException, UnauthorisedException
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

    @Test
    public void shouldIdentifyNonTemplateContextVariables() throws ResourceNotFoundException, UnauthorisedException, InvalidContextParameterException
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        final String[] anotherParamValue = {"88"};

        Map<String, Object> requestParams = ImmutableMap.<String, Object>builder()
                .put("blah.id", new String[]{"10"})
                .put("user.address.street", new String[]{"deadEndSt"})
                .put("notInUrlTemplate", anotherParamValue)
                .build();

        final Set<String> templateVariables = ImmutableSet.of("blah.id", "user.address.street");
        final Map<String, String[]> nonTemplateVariables = ImmutableMap.of("notInUrlTemplate", anotherParamValue);

        when(urlVariableSubstitutor.getContextVariables(pathTemplate)).thenReturn(templateVariables);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(requestParams, "fred")).thenReturn(requestParams);

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, requestParams,
                "fred");

        assertThat(urlTemplateInstance.getNonTemplateContextParameters(), is(equalTo(nonTemplateVariables)));

        verify(urlVariableSubstitutor, times(1)).getContextVariables(pathTemplate);

    }

    @Ignore // Removed support for Json request param form for now
    @Test
    public void shouldIdentifyNonTemplateContextVariablesWhenContextIsJson() throws ResourceNotFoundException, UnauthorisedException, InvalidContextParameterException
    {
        final String pathTemplate = "/foo/bar?arg1=${blah.id}&arg2=${user.address.street}";
        final String[] anotherParamValue = {"88"};

        String[] contextStr = new String[]{"{\"blah\":{\"id\":10100}},\"user\":{\"address\":{\"street\":\"deadEndSt\"}}}"};

//        Map<String, Object> requestParams = ImmutableMap.<String, Object>of("context", contextStr, "someparam", "somevalue");

        Map<String, Object> requestParams = ImmutableMap.<String, Object>builder()
                .put("context", contextStr)
                .put("notInUrlTemplate", anotherParamValue)
                .build();

        final Set<String> templateVariables = ImmutableSet.of("blah.id", "user.address.street");
        final Map<String, String[]> nonTemplateVariables = ImmutableMap.of("notInUrlTemplate", anotherParamValue);

        when(urlVariableSubstitutor.getContextVariables(pathTemplate)).thenReturn(templateVariables);
        when(contextMapURLSerializer.getAuthenticatedAddonParameters(anyMap(), eq("fred"))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[0];
            }
        });

        final UrlTemplateInstance urlTemplateInstance = new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, pathTemplate, requestParams,
                "fred");

        assertThat(urlTemplateInstance.getNonTemplateContextParameters(), is(equalTo(nonTemplateVariables)));

        verify(urlVariableSubstitutor, times(1)).getContextVariables(pathTemplate);

    }
}
