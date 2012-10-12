package com.atlassian.plugin.remotable.spi.http;

import com.atlassian.httpclient.api.EntityBuilder;
import com.atlassian.plugin.remotable.api.service.http.FormBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class TestDefaultFormBuilder
{
    @Test
    public void testOneEmptyParam()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo");
        assertEquals("foo", toString(form));
    }

    @Test
    public void testTwoLikeEmptyParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo");
        form.addParam("foo");
        assertEquals("foo&foo", toString(form));
    }

    @Test
    public void testTwoEmptyParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo");
        form.addParam("bar");
        assertEquals("foo&bar", toString(form));
    }

    @Test
    public void testOneParam()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("foo", "bar");
        assertEquals("foo=bar", toString(form));
    }

    @Test
    public void testTwoLikeParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("one", "a");
        form.addParam("one", "b");
        assertEquals("one=a&one=b", toString(form));
    }

    @Test
    public void testTwoParams()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("one", "1");
        form.addParam("two", "2");
        assertEquals("one=1&two=2", toString(form));
    }

    @Test
    public void testUrlEncoding()
    {
        FormBuilder form = new DefaultFormBuilder();
        form.addParam("one param", "one value");
        form.addParam("two/param", "two/value");
        form.addParam("three∫param", "three∫value");
        form.addParam("four&param", "four&value");
        assertEquals("one+param=one+value&two%2Fparam=two%2Fvalue&three%E2%88%ABparam=three%E2%88%ABvalue&four%26param=four%26value", toString(form));
    }

    @Test
    public void testHeaders()
    {
        FormBuilder form = new DefaultFormBuilder();
        EntityBuilder.Entity entity = form.build();
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", entity.getHeaders().get("Content-Type"));
    }

    private static String toString(FormBuilder form)
    {
        try
        {
            return IOUtils.toString(form.build().getInputStream());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
