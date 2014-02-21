package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.NoAuthRemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.base.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class NoAuthRemotablePluginAccessorTest extends BaseSigningRemotablePluginAccessorTest
{
    private static final URI PATH_URI = URI.create("/path");
    private static final Map<String, String[]> GET_PARAMS_STRING_ARRAY = Collections.singletonMap("param", new String[]{"param value"});

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginKey() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getKey(), is(PLUGIN_KEY));
    }

    @Test
    public void createdRemotePluginAccessorHasCorrectPluginName() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getName(), is(PLUGIN_NAME));
    }

    @Test
    public void createdRemotePluginAccessorCreatesCorrectGetUrl() throws Exception
    {
        assertThat(createRemotePluginAccessor().signGetUrl(PATH_URI, GET_PARAMS_STRING_ARRAY), is(OUTGOING_FULL_GET_URL));
    }

    @Test
    public void createdRemotePluginAccessorCreatesDoesNotSignUrl() throws Exception
    {
        RemotablePluginAccessor remotePluginAccessor = createRemotePluginAccessor();
        String signUrl = remotePluginAccessor.signGetUrl(PATH_URI, GET_PARAMS_STRING_ARRAY);
        String createUrl = remotePluginAccessor.createGetUrl(PATH_URI, GET_PARAMS_STRING_ARRAY);
        assertThat(signUrl, is(createUrl));
    }

    @Test
    public void authorizationGeneratorIsNotNull() throws ExecutionException, InterruptedException
    {
        assertThat(createRemotePluginAccessor().getAuthorizationGenerator(), is(not(nullValue())));
    }

    @Test
    public void testGetAuthorizationGenerator() throws Exception
    {
        Map<String, String[]> params = Collections.singletonMap("param", new String[]{"param value"});
        Option<String> auth = createRemotePluginAccessor().getAuthorizationGenerator().generate(HttpMethod.POST, PATH_URI, params);
        assertThat(auth, is(Option.none(String.class)));
    }

    private RemotablePluginAccessor createRemotePluginAccessor() throws ExecutionException, InterruptedException
    {
        Supplier<URI> baseUrlSupplier = new Supplier<URI>()
        {
            @Override
            public URI get()
            {
                return URI.create(BASE_URL);
            }
        };

        return new NoAuthRemotablePluginAccessor(mockPlugin(), baseUrlSupplier, mockCachingHttpContentRetriever());
    }

    @Override
    protected Map<String, String> getPostSigningHeaders(Map<String, String> preSigningHeaders)
    {
        return preSigningHeaders;
    }
}
