package com.atlassian.plugin.connect.core.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UrlBeanTest
{

    @Test
    public void verifyGetUrl()
    {
        UrlBean bean = new UrlBean("http://example.com/endpoint");
        assertEquals("http://example.com/endpoint", bean.getUrl());
    }

    @Test
    public void verifyCreateAbsoluteUri() throws Exception
    {
        UrlBean bean = new UrlBean("http://example.com/endpoint");
        assertEquals("http://example.com/endpoint", bean.createUri().toString());
    }

    @Test
    public void verifyCreateRelativeUri() throws Exception
    {
        UrlBean bean = new UrlBean("/endpoint");
        assertEquals("/endpoint", bean.createUri().toString());
    }

    @Test
    public void verifyNullUri() throws Exception
    {
        UrlBean bean = new UrlBean(null);
        assertEquals(null, bean.createUri());
    }
}
