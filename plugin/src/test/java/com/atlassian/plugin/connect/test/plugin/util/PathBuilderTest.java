package com.atlassian.plugin.connect.test.plugin.util;

import com.atlassian.plugin.connect.plugin.util.PathBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PathBuilderTest
{
    @Test
    public void testStandardPath()
    {
        String path = new PathBuilder().withPathFragment("/test").build();
        assertThat(path, is("/test"));
    }

    @Test
    public void testNoPrefixPath()
    {
        String path = new PathBuilder().withPathFragment("test").build();
        assertThat(path, is("/test"));
    }

    @Test
    public void testVariation1()
    {
        String path = new PathBuilder().withPathFragment("test").withPathFragment("other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation2()
    {
        String path = new PathBuilder().withPathFragment("/test").withPathFragment("other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation3()
    {
        String path = new PathBuilder().withPathFragment("/test/").withPathFragment("other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation4()
    {
        String path = new PathBuilder().withPathFragment("test").withPathFragment("/other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation5()
    {
        String path = new PathBuilder().withPathFragment("/test").withPathFragment("/other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation6()
    {
        String path = new PathBuilder().withPathFragment("/test/").withPathFragment("/other").build();
        assertThat(path, is("/test/other"));
    }

    @Test
    public void testVariation7()
    {
        String path = new PathBuilder().withPathFragment("test").withPathFragment("/other/").build();
        assertThat(path, is("/test/other/"));
    }

    @Test
    public void testVariation8()
    {
        String path = new PathBuilder().withPathFragment("/test").withPathFragment("/other/").build();
        assertThat(path, is("/test/other/"));
    }

    @Test
    public void testVariation9()
    {
        String path = new PathBuilder().withPathFragment("/test/").withPathFragment("/other/").build();
        assertThat(path, is("/test/other/"));
    }


    @Test
    public void testFullUrl()
    {
        String path = new PathBuilder().withBaseUrl("http://example.com/").withPathFragment("/other/").build();
        assertThat(path, is("http://example.com/other/"));
    }

}
