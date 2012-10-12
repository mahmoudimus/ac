package com.atlassian.plugin.remotable.host.common.service.confluence;

import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceLabelClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePageClient;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluencePermission;
import com.atlassian.plugin.remotable.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentPermission;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentPermissionSet;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentPermissionType;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentStatus;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ExportType;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableContentPermission;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableLabel;
import com.atlassian.plugin.remotable.api.service.confluence.domain.Page;
import com.atlassian.plugin.remotable.api.service.confluence.domain.PageSummary;
import com.atlassian.plugin.remotable.api.service.confluence.domain.SpacePermission;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.plugin.remotable.api.service.http.HostXmlRpcClient;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.util.ChainingClassLoader;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promises;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.atlassian.plugin.remotable.api.service.confluence.domain.ConfluenceDomain.*;
import static com.atlassian.plugin.remotable.host.common.service.confluence.ClientInvocationHandler.*;
import static com.google.common.collect.Sets.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

/**
 */
public class TestClientInvocationHandler
{
    private ConfluencePageClient confluencePageClient;
    private HostXmlRpcClient client;
    private HostHttpClient httpClient;
    private RequestContext requestContext;
    private ConfluenceLabelClient confluenceLabelClient;
    private ConfluenceSpaceClient confluenceSpaceClient;

    private static final Set<String> permissions = newHashSet(
            ConfluencePermission.READ_CONTENT,
            ConfluencePermission.MODIFY_SPACES,
            ConfluencePermission.MODIFY_CONTENT,
            ConfluencePermission.LABEL_CONTENT);

    @Before
    public void setUp()
    {
        client = mock(HostXmlRpcClient.class);
        httpClient = mock(HostHttpClient.class);
        requestContext = mock(RequestContext.class);
        confluencePageClient = (ConfluencePageClient) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader()),
                new Class[]{ConfluencePageClient.class},
                new ClientInvocationHandler("confluence2", client, permissions, "foo", httpClient,
                        requestContext));
        confluenceLabelClient = (ConfluenceLabelClient) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader()),
                new Class[]{ConfluenceLabelClient.class},
                new ClientInvocationHandler("confluence2", client, permissions, "foo", httpClient,
                        requestContext));
        confluenceSpaceClient = (ConfluenceSpaceClient) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader()),
                new Class[]{ConfluenceSpaceClient.class},
                new ClientInvocationHandler("confluence2", client, permissions, "foo", httpClient,
                        requestContext));
    }

    @Test
    public void testFlatResponse()
    {
        Map data = ImmutableMap.builder()
                .put("id", "100")
                .put("space", "DS")
                .put("title", "Title")
                .put("url", "http://example.com/foo")
                .build();
        when(client.invoke("confluence2.getPageSummary", Object.class, "", "100")).thenReturn(Promises.<Object>toResolvedPromise(data));

        PageSummary page = confluencePageClient.getPageSummary(100L).claim();
        assertEquals(100L, page.getId());
        assertEquals("DS", page.getSpaceKey());
        assertEquals(URI.create("http://example.com/foo"), page.getUrl());
        assertEquals("Title", page.getTitle());
    }

    @Test
    public void testObjectInArgument()
    {
        Map data = ImmutableMap.builder()
                .put("id", "100")
                .build();
        when(client.invoke("confluence2.addLabelByObject", Object.class, "", data, "200")).thenReturn(Promises.<Object>toResolvedPromise(data));

        MutableLabel label = newLabel();
        label.setId(100);
        confluenceLabelClient.addLabelByObject(label, 200);
        verify(client, atLeastOnce()).invoke("confluence2.addLabelByObject", Object.class, "", data, "200");
    }

    @Test
    public void testEnumInRequest()
    {
        when(client.invoke("confluence2.removePermissionFromSpace", Object.class, "", "COMMENT", "entityName", "DS")).thenReturn(Promises.<Object>toResolvedPromise(null));

        confluenceSpaceClient.removePermissionFromSpace(SpacePermission.COMMENT_PERMISSION, "entityName", "DS");
        verify(client, atLeastOnce()).invoke("confluence2.removePermissionFromSpace", Object.class, "", "COMMENT", "entityName", "DS");
    }

    @Test(expected = PermissionDeniedException.class)
    public void testPermissionViolation()
    {
        confluenceSpaceClient.removeAnonymousUsePermission();
    }

    @Test
    public void testIterableInRequest()
    {
        List<Map> permissions = Arrays.<Map>asList(ImmutableMap.builder()
                .put("type", "View")
                .put("userName", "bob")
                .build()
        );
        when(client.invoke(eq("confluence2.setContentPermissions"), eq(Object.class), eq(""),
                eq("100"), eq("View"),
                argThat(new CollectionsMatcher(permissions)))).thenReturn(Promises.<Object>toResolvedPromise(null));

        MutableContentPermission permission = newContentPermission();
        permission.setUserName("bob");
        permission.setType(ContentPermissionType.VIEW);
        confluencePageClient.setContentPermissions(100L, ContentPermissionType.VIEW,
                asList(permission));
        verify(client, only()).invoke(eq("confluence2.setContentPermissions"), eq(Object.class), eq(""),
                eq("100"), eq("View"),
                argThat(new CollectionsMatcher(permissions)));
    }

    @Test
    public void testIterableInResponse()
    {
        Map contentPermissionSet = ImmutableMap.builder()
                .put("contentPermissions", Arrays.<Map>asList(ImmutableMap.builder()
                    .put("type", "View")
                    .put("userName", "bob")
                    .build()))
                .put("type", "View")
                .build();
        when(client.invoke(eq("confluence2.getContentPermissionSet"), eq(Object.class), eq(""),
                eq("100"), eq("View"))).thenReturn(Promises.<Object>toResolvedPromise(contentPermissionSet));

        MutableContentPermission permission = newContentPermission();
        permission.setUserName("bob");
        permission.setType(ContentPermissionType.VIEW);
        ContentPermissionSet set = confluencePageClient.getContentPermissionSet(100L, ContentPermissionType.VIEW).claim();
        assertNotNull(set);
        assertEquals(set.getType(), ContentPermissionType.VIEW);
        ContentPermission contentPermission = set.getContentPermissions().iterator().next();
        assertEquals(contentPermission.getUserName(), "bob");
        assertEquals(contentPermission.getGroupName(), null);
        assertEquals(contentPermission.getType(), ContentPermissionType.VIEW);
    }

    @Test
    public void testInputStreamInResponse() throws IOException, NoSuchFieldException
    {
        when(requestContext.getHostBaseUrl()).thenReturn("http://localhost");
        when(client.invoke("confluence2.exportSpace", Object.class, "",
                "DS", getEnumRemoteName(ExportType.HTML))).thenReturn(Promises.<Object>toResolvedPromise("http://localhost/export"));
        Request request = mock(Request.class);
        when(httpClient.newRequest("/export"))
                .thenReturn(request);
        final Response response = mock(Response.class);

        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        when(response.getEntityStream()).thenReturn(bin);
        final ResponsePromise responsePromise = mock(ResponsePromise.class);
        when(request.get()).thenReturn(responsePromise);
        when(responsePromise.others(any(Effect.class))).thenReturn(responsePromise);
        when(responsePromise.fail(any(Effect.class))).thenReturn(responsePromise);
        when(responsePromise.ok(any(Effect.class))).then(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                Effect<Response> callback = (Effect<Response>) invocation
                        .getArguments()[0];
                callback.apply(response);
                return responsePromise;
            }
        });

        assertEquals(bin, confluenceSpaceClient.exportSpace("DS", ExportType.HTML).claim());
    }

    @Test
    public void testEmptyIterableInResponse()
    {
        Map contentPermissionSet = ImmutableMap.builder()
                .put("type", "View")
                .build();
        when(client.invoke(eq("confluence2.getContentPermissionSet"), eq(Object.class), eq(""),
                eq("100"), eq("View"))).thenReturn(Promises.<Object>toResolvedPromise(contentPermissionSet));

        MutableContentPermission permission = newContentPermission();
        permission.setUserName("bob");
        permission.setType(ContentPermissionType.VIEW);
        ContentPermissionSet set = confluencePageClient.getContentPermissionSet(100L, ContentPermissionType.VIEW).claim();
        assertNotNull(set);
        assertEquals(set.getType(), ContentPermissionType.VIEW);
        assertFalse(set.getContentPermissions().iterator().hasNext());
    }

    @Test
    public void testEnumInResponseObject()
    {
        Map data = ImmutableMap.builder()
                .put("id", "100")
                .put("space", "DS")
                .put("title", "Title")
                .put("url", "http://example.com/foo")
                .put("contentStatus", "current")
                .build();
        when(client.invoke("confluence2.getPage", Object.class, "", "100")).thenReturn(Promises.<Object>toResolvedPromise(data));

        Page page = confluencePageClient.getPage(100L).claim();
        assertEquals(100L, page.getId());
        assertEquals("DS", page.getSpaceKey());
        assertEquals(URI.create("http://example.com/foo"), page.getUrl());
        assertEquals("Title", page.getTitle());
        assertEquals(ContentStatus.CURRENT, page.getContentStatus());
    }

    @Test
    public void testListResponse()
    {
        Map data = ImmutableMap.builder()
                .put("id", "100")
                .put("space", "DS")
                .put("title", "Title")
                .put("url", "http://example.com/foo")
                .build();
        when(client.invoke("confluence2.getPages", Object.class, "", "DS")).thenReturn(Promises.<Object>toResolvedPromise(singletonList(data)));

        Iterable<PageSummary> pageIterable = confluencePageClient.getPages("DS").claim();
        assertNotNull(pageIterable);
        PageSummary page = pageIterable.iterator().next();
        assertEquals(100L, page.getId());
        assertEquals("DS", page.getSpaceKey());
        assertEquals(URI.create("http://example.com/foo"), page.getUrl());
        assertEquals("Title", page.getTitle());
    }

    private class CollectionsMatcher extends BaseMatcher<Object>
    {
        private final Object my;

        public CollectionsMatcher(Object my)
        {
            this.my = my;
        }

        @Override
        public boolean matches(Object o)
        {
            return match(my, o);
        }

        private boolean match(Object self, Object target)
        {
            if (target instanceof Collection && self instanceof Collection)
            {
                Iterator targetIterator = ((Iterable)target).iterator();
                Iterator selfIterator = ((Iterable)self).iterator();
                while (targetIterator.hasNext())
                {
                    if (!match(selfIterator.next(), targetIterator.next()))
                        return false;
                }
                return !targetIterator.hasNext() && !selfIterator.hasNext();
            }
            else if (target instanceof Map && self instanceof Map)
            {
                Iterator<Map.Entry> targetIterator = new TreeMap(((Map)target)).entrySet().iterator();
                Iterator<Map.Entry> selfIterator = new TreeMap(((Map)self)).entrySet().iterator();
                while (targetIterator.hasNext())
                {
                    if (!match(selfIterator.next(), targetIterator.next()))
                    {
                        return false;
                    }
                }
                return !targetIterator.hasNext() && !selfIterator.hasNext();
            }
            else
            {
                return self.equals(target);
            }
        }

        @Override
        public void describeTo(Description description)
        {
        }
    }
}
