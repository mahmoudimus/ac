package com.atlassian.plugin.connect.plugin.module.context;


import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
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
public class ContextMapURLDeSerializerTest
{
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextMapParameterExtractor parameterExtractor1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextMapParameterExtractor parameterExtractor2;

    private Object extracted1 = new Object();
    private Object extracted2 = new Object();

    @Test
    public void shouldAuthoriseAllResourceRefs() throws ResourceNotFoundException, UnauthorisedException
    {
        final ContextMapURLSerializer urlSerializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1, parameterExtractor2));
        final ImmutableMap<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", 10,
                "key2", "blah",
                "key3", 22.2);


        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenReturn(Optional.of(extracted1));
        when(parameterExtractor2.deserializer().deserialize(context, "fred")).thenReturn(Optional.of(extracted2));

        when(parameterExtractor1.serializer().serialize(extracted1)).thenReturn(
                ImmutableMap.<String, Object>of("new key1", "value1"));
        when(parameterExtractor2.serializer().serialize(extracted2)).thenReturn(
                ImmutableMap.<String, Object>of("new key2", 22.2));

//        when(parameterExtractor1.hasViewPermission(anyString(), any())).thenReturn(true);
//        when(parameterExtractor2.hasViewPermission(anyString(), any())).thenReturn(true);


        final Map<String, Object> parameters = urlSerializer.getAuthenticatedAddonParameters(context, "fred");

        verify(parameterExtractor1.deserializer(), times(1)).deserialize(context, "fred");
        verify(parameterExtractor2.deserializer(), times(1)).deserialize(context, "fred");

        verify(parameterExtractor1.serializer(), times(1)).serialize(extracted1);
        verify(parameterExtractor2.serializer(), times(1)).serialize(extracted2);

        assertThat(parameters, hasEntry("new key1", (Object) "value1"));
        assertThat(parameters, hasEntry("new key2", (Object) 22.2));
        assertThat(parameters.entrySet(), hasSize(2));
    }

    /* cases
        - extra params (variables)
        - all resource refs must be validated
     */

    @Test
    @Ignore // TODO: We may need to bring this back when we do permission checks in confluence.
    public void shouldExcludeParametersThatUserDoesNotHaveViewPermissionFor() throws ResourceNotFoundException, UnauthorisedException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", 10,
                "key2", "blah");

        // TODO: jira deserializers already authorise. Confluence doesn't. How should we handle?
        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenReturn(Optional.of(extracted1));

        when(parameterExtractor1.serializer().serialize(extracted1)).thenReturn(
                ImmutableMap.<String, Object>of("new key1", "value1"));

        when(parameterExtractor1.hasViewPermission("fred", extracted1)).thenReturn(false);

        final Map<String, Object> parameters = serializer.getAuthenticatedAddonParameters(context, "fred");

        verify(parameterExtractor1, times(1)).hasViewPermission("fred", extracted1);

        assertThat(parameters.isEmpty(), is(true));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldLetResourceNotFoundExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of();

        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenThrow(new ResourceNotFoundException("blah"));

        serializer.getAuthenticatedAddonParameters(context, "fred");
    }

    @Test(expected = MalformedRequestException.class)
    public void shouldLetMalformedRequestExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of();

        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenThrow(new MalformedRequestException("blah"));

        serializer.getAuthenticatedAddonParameters(context, "fred");
    }

    @Test(expected = UnauthorisedException.class)
    public void shouldLetUnauthorisedExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of();

        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenThrow(new UnauthorisedException("blah"));

        serializer.getAuthenticatedAddonParameters(context, "fred");
    }
}
