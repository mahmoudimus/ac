package com.atlassian.plugin.connect.plugin.module.context;


import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContextMapURLSerializerTest
{
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextMapParameterExtractor parameterExtractor1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextMapParameterExtractor parameterExtractor2;

    private Object extracted1 = new Object();
    private Object extracted2 = new Object();

    @Test
    public void shouldIncludeOnlyEntriesProvidedByExtractors()
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1, parameterExtractor2));
        final ImmutableMap<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", 10,
                "key2", "blah",
                "key3", 22.2);


        when(parameterExtractor1.extract(context)).thenReturn(Optional.of(extracted1));
        when(parameterExtractor2.extract(context)).thenReturn(Optional.of(extracted2));
        when(parameterExtractor1.serializer().serialize(extracted1)).thenReturn(
                ImmutableMap.<String, Object>of("new key1", "value1"));
        when(parameterExtractor2.serializer().serialize(extracted2)).thenReturn(
                ImmutableMap.<String, Object>of("new key2", 22.2));
        when(parameterExtractor1.hasViewPermission(anyString(), any())).thenReturn(true);
        when(parameterExtractor2.hasViewPermission(anyString(), any())).thenReturn(true);


        final Map<String, Object> parameters = serializer.getExtractedWebPanelParameters(context, "barney");

        verify(parameterExtractor1, times(1)).extract(context);
        verify(parameterExtractor2, times(1)).extract(context);

        assertThat(parameters, hasEntry("new key1", (Object) "value1"));
        assertThat(parameters, hasEntry("new key2", (Object) 22.2));
        assertThat(parameters.entrySet(), hasSize(2));
    }

    @Test
    public void shouldExcludeParametersThatUserDoesNotHaveViewPermissionFor()
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final ImmutableMap<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", 10,
                "key2", "blah");


        when(parameterExtractor1.extract(context)).thenReturn(Optional.of(extracted1));
        when(parameterExtractor1.serializer().serialize(extracted1)).thenReturn(
                ImmutableMap.<String, Object>of("new key1", "value1"));

        when(parameterExtractor1.hasViewPermission("fred", extracted1)).thenReturn(false);

        final Map<String, Object> parameters = serializer.getExtractedWebPanelParameters(context, "fred");

        verify(parameterExtractor1, times(1)).hasViewPermission("fred", extracted1);

        assertThat(parameters.isEmpty(), is(true));
    }
}
