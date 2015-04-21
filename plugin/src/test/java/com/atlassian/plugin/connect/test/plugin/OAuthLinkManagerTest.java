package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.ServiceProvider;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.oauth.OAuthMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith (MockitoJUnitRunner.class)
public class OAuthLinkManagerTest
{
    @InjectMocks private OAuthLinkManager oAuthLinkManager;

    @Mock private ConsumerService consumerService;

    // unmockable, unlovable final classes :(
    private final ServiceProvider serviceProvider = new ServiceProvider(
        URI.create("http://example.com/req"),
        URI.create("http://example.com/authz"),
        URI.create("http://example.com/access")
    );
    private final Consumer consumer = Consumer
            .key("key")
            .name("name")
            .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1)
            .build();

    @Before
    public void setUp()
    {
        when(consumerService.getConsumer()).thenReturn(consumer);
        when(consumerService.sign(any(Request.class), eq(serviceProvider))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                return invocation.getArguments()[0];
            }
        });
    }

    @Test
    public void testSign()
    {
        final String url = "https://how.the/path/ends";
        final Map<String, List<String>> params = ImmutableMap.<String, List<String>>of
                ("key", ImmutableList.of("val", "sal"),
                 "kev", ImmutableList.of("ian", "jan"));

        OAuthMessage message = oAuthLinkManager.sign(serviceProvider, HttpMethod.GET, URI.create(url), params);

        assertThat(message.URL, is(url));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSignThrowsIfUrlIsNotNormalized()
    {
        final String url = "https://how.the/path/ends/../or/not";
        final Map<String, List<String>> params = ImmutableMap.<String, List<String>>of
                ("key", ImmutableList.of("val", "sal"),
                 "kev", ImmutableList.of("ian", "jan"));

        oAuthLinkManager.sign(serviceProvider, HttpMethod.GET, URI.create(url), params); // this should throw IAE
    }

}
