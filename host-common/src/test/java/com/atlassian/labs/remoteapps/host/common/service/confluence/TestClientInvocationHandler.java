package com.atlassian.labs.remoteapps.host.common.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceLabelClient;
import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePageClient;
import com.atlassian.labs.remoteapps.api.service.confluence.ConfluenceSpaceClient;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.*;
import com.atlassian.labs.remoteapps.api.service.http.HostXmlRpcClient;
import com.atlassian.labs.remoteapps.spi.Promises;
import com.atlassian.plugin.util.ChainingClassLoader;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.*;

import static com.atlassian.labs.remoteapps.api.service.confluence.domain.ConfluenceDomain
        .newContentPermission;
import static com.atlassian.labs.remoteapps.api.service.confluence.domain.ConfluenceDomain.newLabel;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 */
public class TestClientInvocationHandler
{

    private ConfluencePageClient confluencePageClient;
    private HostXmlRpcClient client;
    private ConfluenceLabelClient confluenceLabelClient;
    private ConfluenceSpaceClient confluenceSpaceClient;

    @Before
    public void setUp()
    {
        client = mock(HostXmlRpcClient.class);
        confluencePageClient = (ConfluencePageClient) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader()),
                new Class[]{ConfluencePageClient.class},
                new ClientInvocationHandler("confluence2", client));
        confluenceLabelClient = (ConfluenceLabelClient) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader()),
                new Class[]{ConfluenceLabelClient.class},
                new ClientInvocationHandler("confluence2", client));
        confluenceSpaceClient = (ConfluenceSpaceClient) Proxy.newProxyInstance(
                new ChainingClassLoader(getClass().getClassLoader()),
                new Class[]{ConfluenceSpaceClient.class},
                new ClientInvocationHandler("confluence2", client));
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
        when(client.invoke("confluence2.getPageSummary", Object.class, "", "100")).thenReturn(
                Promises.<Object>ofInstance(data));

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
        when(client.invoke("confluence2.addLabelByObject", Object.class, "", data, "200")).thenReturn(Promises.<Object>ofInstance(data));

        MutableLabel label = newLabel();
        label.setId(100);
        confluenceLabelClient.addLabelByObject(label, 200);
        verify(client, atLeastOnce()).invoke("confluence2.addLabelByObject", Object.class, "", data, "200");
    }

    @Test
    public void testEnumInRequest()
    {
        when(client.invoke("confluence2.removeGlobalPermission", Object.class, "", "USECONFLUENCE", "entityName")).thenReturn(
                Promises.<Object>ofInstance(null));

        confluenceSpaceClient.removeGlobalPermission(GlobalPermission.USE_CONFLUENCE_PERMISSION,
                "entityName");
        verify(client, atLeastOnce()).invoke("confluence2.removeGlobalPermission", Object.class, "", "USECONFLUENCE", "entityName");
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
                argThat(new CollectionsMatcher(permissions)))).thenReturn(
                Promises.<Object>ofInstance(null));

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
                eq("100"), eq("View"))).thenReturn(
                Promises.<Object>ofInstance(contentPermissionSet));

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
    public void testEmptyIterableInResponse()
    {
        Map contentPermissionSet = ImmutableMap.builder()
                .put("type", "View")
                .build();
        when(client.invoke(eq("confluence2.getContentPermissionSet"), eq(Object.class), eq(""),
                eq("100"), eq("View"))).thenReturn(
                Promises.<Object>ofInstance(contentPermissionSet));

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
        when(client.invoke("confluence2.getPage", Object.class, "", "100")).thenReturn(
                Promises.<Object>ofInstance(data));

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
        when(client.invoke("confluence2.getPages", Object.class, "", "DS")).thenReturn(
                Promises.<Object>ofInstance(singletonList(data)));

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
