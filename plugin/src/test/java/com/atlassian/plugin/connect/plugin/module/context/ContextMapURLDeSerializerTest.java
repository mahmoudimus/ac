package com.atlassian.plugin.connect.plugin.module.context;


import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
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
    public void shouldAuthoriseAllResourceRefs() throws ResourceNotFoundException, UnauthorisedException, MalformedRequestException
    {
        final ContextMapURLSerializer urlSerializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1, parameterExtractor2));
        final Map<String, Object> context = ImmutableMap.<String, Object>of(
                "key1", 10,
                "key2", "blah",
                "key3", 22.2);


        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenReturn(Optional.of(extracted1));
        when(parameterExtractor2.deserializer().deserialize(context, "fred")).thenReturn(Optional.of(extracted2));

        final Map<String, Object> parameters = urlSerializer.getAuthenticatedAddonParameters(context, "fred");

        verify(parameterExtractor1.deserializer(), times(1)).deserialize(context, "fred");
        verify(parameterExtractor2.deserializer(), times(1)).deserialize(context, "fred");

        assertThat(parameters, is(equalTo(context)));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldLetResourceNotFoundExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException, MalformedRequestException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of();

        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenThrow(new ResourceNotFoundException("blah"));

        serializer.getAuthenticatedAddonParameters(context, "fred");
    }

    @Test(expected = MalformedRequestException.class)
    public void shouldLetMalformedRequestExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException, MalformedRequestException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of();

        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenThrow(new MalformedRequestException("blah"));

        serializer.getAuthenticatedAddonParameters(context, "fred");
    }

    @Test(expected = UnauthorisedException.class)
    public void shouldLetUnauthorisedExceptionPassThrough() throws ResourceNotFoundException, UnauthorisedException, MalformedRequestException
    {
        final ContextMapURLSerializer serializer = new ContextMapURLSerializer(ImmutableList.of(parameterExtractor1));
        final Map<String, Object> context = ImmutableMap.<String, Object>of();

        when(parameterExtractor1.deserializer().deserialize(context, "fred")).thenThrow(new UnauthorisedException("blah"));

        serializer.getAuthenticatedAddonParameters(context, "fred");
    }
}
